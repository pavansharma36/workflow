package org.one.workflow.api.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.one.workflow.api.WorkflowListener;
import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.WorkflowManagerListener;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.bean.RunEvent;
import org.one.workflow.api.bean.State;
import org.one.workflow.api.bean.id.RunId;
import org.one.workflow.api.bean.id.TaskId;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskImplType;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.dag.RunnableTaskDagBuilder;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.executor.TaskExecutor;
import org.one.workflow.api.model.ManagerInfo;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;
import org.one.workflow.api.queue.QueueConsumer;
import org.one.workflow.api.schedule.Scheduler;
import org.one.workflow.api.util.WorkflowException;

/**
 * default implementation for {@link WorkflowManager}.
 */
@Slf4j
public class WorkflowManagerImpl implements WorkflowManager {

  private final WorkflowManagerListener workflowManagerListener = new WorkflowManagerListener();
  private final WorkflowAdapter adapter;
  private final ExecutorService executorService;
  private final ScheduledExecutorService scheduledExecutorService;
  private final Scheduler scheduler;
  private final QueueConsumer queueConsumer;
  private final ManagerInfo managerInfo = ManagerInfo.getInstance();
  private final AtomicReference<State> state = new AtomicReference<>(State.INIT);

  protected WorkflowManagerImpl(final WorkflowAdapter adapter,
                                final ExecutorService executorService,
                                final ScheduledExecutorService scheduledExecutorService,
                                final List<TaskDefination> taskDefinations) {
    super();
    this.adapter = adapter;
    this.executorService = executorService;
    this.scheduledExecutorService = scheduledExecutorService;
    this.scheduler = new Scheduler(adapter);
    this.queueConsumer = new QueueConsumerImpl(adapter, taskDefinations);
  }

  @Override
  public void close() throws IOException {
    if (state.compareAndSet(State.STARTED, State.STOPPING)) {
      log.info("Stopping scheduler");
      scheduler.stop();

      log.info("Stopping adapters");
      adapter.stop();

      log.info("Shutting down scheduled executor service");
      scheduledExecutorService().shutdown();

      log.info("Shutting down executor service");
      executorService().shutdown();

      try {
        if (scheduledExecutorService().awaitTermination(3, TimeUnit.SECONDS)) {
          log.info("Successfully stopped scheduled executor service");
        } else {
          List<Runnable> list = scheduledExecutorService().shutdownNow();
          log.info("Force shutdown scheduled executor service, dropped {} tasks", list.size());
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      try {
        if (executorService().awaitTermination(30, TimeUnit.SECONDS)) {
          log.info("Successfully stopped executor service");
        } else {
          List<Runnable> list = executorService().shutdownNow();
          log.info("Force shutdown executor service, dropped {} tasks", list.size());
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      state.compareAndSet(State.STOPPING, State.STOPPED);
    } else {
      throw new WorkflowException("Not valid state " + state.get());
    }
  }

  @Override
  public void start() {
    if (state.compareAndSet(State.INIT, State.STARTING)) {
      log.info("Starting workflow");
      adapter.start(this);

      scheduler.start(this);

      queueConsumer.start(this);

      state.compareAndSet(State.STARTING, State.STARTED);
    } else {
      throw new WorkflowException("Not valid state " + state.get());
    }
  }

  @Override
  public ManagerInfo info() {
    return managerInfo;
  }

  @Override
  public RunId submit(final Task root) {
    return submit(new RunId(), root);
  }

  @Override
  public RunId submit(final RunId runId, final Task root) {
    assertRunning();

    log.info("Submitting run {}", runId);
    final RunnableTaskDagBuilder builder = new RunnableTaskDagBuilder(root);

    final RunInfo runInfo = new RunInfo();
    runInfo.setRunId(runId);
    runInfo.setQueuedTime(System.currentTimeMillis());
    runInfo.setDag(builder.getEntries());
    runInfo.setLastUpdateEpoch(System.currentTimeMillis());
    runInfo.setQueuedBy(info().getManagerId());

    adapter.persistenceAdapter().createRunInfo(runInfo);

    adapter.persistenceAdapter().createTaskInfos(runId,
        builder.getTasks().values().stream().map(t -> new TaskInfo(runId, t))
            .collect(Collectors.toList()));

    // for scheduler to pick.
    adapter.queueAdapter().pushUpdatedRun(runId);

    return runId;
  }

  @Override
  public ScheduledExecutorService scheduledExecutorService() {
    return scheduledExecutorService;
  }

  @Override
  public ExecutorService executorService() {
    return executorService;
  }

  @Override
  public boolean cancelRun(final RunId runId) {
    assertRunning();
    boolean cleanup = adapter.persistenceAdapter().cleanup(runId);
    if (cleanup) {
      workflowManagerListener().publishEvent(
          new RunEvent(runId, WorkflowListener.RunEventType.RUN_ABORTED));
    }
    return cleanup;
  }

  @Override
  public boolean completeAsyncTask(final RunId runId, final TaskId taskId,
                                   final ExecutionResult executionResult) {
    if ((executionResult == null) || (executionResult.getStatus() == null)) {
      throw new WorkflowException("Result cannot be null");
    }
    assertRunning();

    final Optional<TaskInfo> oTaskInfo = adapter.persistenceAdapter().getTaskInfo(runId, taskId);
    if (oTaskInfo.isPresent()) {
      TaskInfo taskInfo = oTaskInfo.get();
      if (taskInfo.getTaskImplType() != TaskImplType.ASYNC) {
        throw new WorkflowException("Only async task types can be marked completed");
      } else if (taskInfo.getStartTimeEpoch() <= 0) {
        throw new WorkflowException("Task is not started yet");
      } else if (adapter.persistenceAdapter()
          .completeTask(ExecutableTask.builder().runId(runId).taskId(taskId).build(),
              executionResult)) {
        adapter.queueAdapter().pushUpdatedRun(runId);
        return true;
      }
    }
    return false;
  }

  @Override
  public Optional<ExecutionResult> getTaskExecutionResult(final RunId runId, final TaskId taskId) {
    assertRunning();
    return adapter.persistenceAdapter().getTaskInfo(runId, taskId).map(TaskInfo::getResult);
  }

  @Override
  public WorkflowManagerListener workflowManagerListener() {
    return workflowManagerListener;
  }

  private void assertRunning() {
    if (state.get() != State.STARTED) {
      throw new WorkflowException("Workflow manager is not in running state");
    }
  }

  /**
   * task defination is required while building workflow manager.
   * with details like how many threads to process given task type.
   * and executor to process task.
   *
   */
  @Getter
  @Setter
  @Builder
  protected static class TaskDefination {
    private TaskType taskType;
    private int threads;
    private TaskExecutor taskExecutor;
    private ExecutorService executorService;
  }

}
