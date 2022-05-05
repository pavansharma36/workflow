package org.one.workflow.api.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.one.workflow.api.WorkflowListener.TaskEventType;
import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.bean.State;
import org.one.workflow.api.bean.TaskEvent;
import org.one.workflow.api.bean.id.RunId;
import org.one.workflow.api.bean.id.TaskId;
import org.one.workflow.api.bean.task.TaskImplType;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.executor.TaskExecutionStatus;
import org.one.workflow.api.impl.WorkflowManagerImpl.TaskDefination;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;
import org.one.workflow.api.queue.QueueConsumer;
import org.one.workflow.api.util.RoundRobinIterator;
import org.one.workflow.api.util.Utils;
import org.one.workflow.api.util.WorkflowException;

@Slf4j
public class QueueConsumerImpl implements QueueConsumer {

  private final AtomicReference<State> state = new AtomicReference<>(State.INIT);
  private final WorkflowAdapter adapter;
  private final Map<TaskType, TaskDefination> taskMap = new HashMap<>();
  private final Map<TaskType, AtomicInteger> inProgress = new HashMap<>();
  private final RoundRobinIterator<TaskType> taskTypeIterator;

  protected QueueConsumerImpl(final WorkflowAdapter adapter,
                              final List<WorkflowManagerImpl.TaskDefination> taskDefinations) {
    this.adapter = adapter;
    taskDefinations.forEach(td -> taskMap.put(td.getTaskType(), td));
    taskTypeIterator = new RoundRobinIterator<>(new ArrayList<>(taskMap.keySet()));
  }

  @Override
  public void start(final WorkflowManager workflowManager) {
    if (state.compareAndSet(State.INIT, State.STARTING)) {
      if (taskTypeIterator.hasNext()) {
        run(workflowManager);
      }

      state.compareAndSet(State.STARTING, State.STARTED);
    } else {
      throw new WorkflowException("Invalid state");
    }
  }

  private void run(final WorkflowManager workflowManager) {
    final TaskType taskType = taskTypeIterator.next();

    boolean result = false;
    try {
      if (inProgress.computeIfAbsent(taskType, t -> new AtomicInteger(0)).get()
          < taskMap.get(taskType).getThreads()) {
        final Optional<ExecutableTask> oTask = adapter.queueAdapter().pollTask(taskType);
        if (oTask.isPresent()) {
          result = true;
          inProgress.computeIfAbsent(taskType, t -> new AtomicInteger(0)).incrementAndGet();

          ExecutorService executorService;
          if (taskMap.get(taskType).getExecutorService() != null) {
            executorService = taskMap.get(taskType).getExecutorService();
          } else {
            executorService = workflowManager.executorService();
          }
          executorService.submit(() -> {
            try {
              final ExecutableTask task = oTask.get();
              final Optional<TaskInfo> oTaskInfo = adapter.persistenceAdapter()
                  .getTaskInfo(task.getRunId(), task.getTaskId());
              if (oTaskInfo.isPresent()) {
                final TaskInfo taskInfo = oTaskInfo.get();

                ExecutionResult executionResult;

                int retry = 0;

                boolean force;
                boolean publishStartEvent = taskInfo.getStartTimeEpoch() <= 0L;
                do {
                  try {
                    if (taskInfo.getTaskImplType() != TaskImplType.IDEMPOTENT
                        && (taskInfo.getStartTimeEpoch() > 0)) {
                      throw new WorkflowException(
                          "Task was started previously and not idempotent");
                    }

                    if (publishStartEvent
                        && adapter.persistenceAdapter().updateStartTime(task.getRunId(),
                            task.getTaskId(), workflowManager.info().getManagerId())) {
                      workflowManager.workflowManagerListener().publishEvent(new TaskEvent(
                          task.getRunId(), task.getTaskId(), TaskEventType.TASK_STARTED));
                      publishStartEvent = false;
                    }

                    executionResult = taskMap.get(taskType).getTaskExecutor().execute(
                        workflowManager,
                        ExecutableTask.builder().runId(task.getRunId())
                            .taskId(task.getTaskId())
                            .taskType(taskInfo.getType())
                            .taskMeta(taskInfo.getTaskMeta()).build());

                    if ((executionResult == null) || (executionResult.getStatus() == null)) {
                      throw new WorkflowException("Result cannot be null");
                    }

                    if (taskInfo.getTaskImplType() == TaskImplType.DECISION
                        && ((executionResult.getDecision() == null)
                        || !validateDecision(task.getRunId(), task.getTaskId(),
                        executionResult.getDecision()))) {
                      throw new WorkflowException("Decision cannot be null");
                    }

                    force = false;
                  } catch (final Throwable e) {
                    log.error("Unhandled error in task execution {}", e.getMessage(), e);
                    executionResult = ExecutionResult.builder()
                        .status(TaskExecutionStatus.FAILED_STOP).message(e.getMessage())
                        .build();
                    force = true;
                  }
                } while (retry++ < taskInfo.getRetryCount());

                if ((taskInfo.getTaskImplType() != TaskImplType.ASYNC || force)
                    && (adapter.persistenceAdapter().completeTask(task, executionResult))) {
                  adapter.queueAdapter().pushUpdatedRun(task.getRunId());

                  workflowManager.workflowManagerListener()
                      .publishEvent(new TaskEvent(task.getRunId(), task.getTaskId(),
                          executionResult.getStatus() == TaskExecutionStatus.SUCCESS
                              ? TaskEventType.TASK_COMPLETED
                              : TaskEventType.TASK_FAILED));
                }
              }

              if (adapter.queueAdapter().commitTaskProcessed(task)) {
                log.debug("Commited task processed {}", task);
              } else {
                log.info("Failed to commit task processed {}", task);
              }
            } finally {
              inProgress.computeIfAbsent(taskType, t -> new AtomicInteger(0)).decrementAndGet();
            }
          });
        }
      }
    } finally {
      if (state.get() != State.STOPPED) {
        final Duration delay = adapter.queueAdapter().pollDelayGenerator().delay(result);
        workflowManager.scheduledExecutorService().schedule(() ->
            run(workflowManager), delay.toMillis(), TimeUnit.MILLISECONDS);
      }
    }
  }

  private boolean validateDecision(final RunId runId, final TaskId taskId, final TaskId decision) {
    final Optional<RunInfo> oRunInfo = adapter.persistenceAdapter().getRunInfo(runId);

    return oRunInfo.isPresent()
        && oRunInfo.flatMap(
                ri -> ri.getDag().stream().filter(d -> d.getTaskId().equals(taskId)).findAny())
            .map(dag -> Utils.nullSafe(dag.getChildrens()).stream()
                .anyMatch(t -> t.equals(decision)))
            .orElseThrow(() -> new RuntimeException("Invalid runid/taskid/decision"));
  }

  @Override
  public void stop() {
    if (!state.compareAndSet(State.STARTED, State.STOPPED)) {
      throw new WorkflowException("Invalid state");
    }
  }

}
