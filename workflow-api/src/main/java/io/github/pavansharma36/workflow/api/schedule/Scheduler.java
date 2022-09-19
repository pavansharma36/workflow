package io.github.pavansharma36.workflow.api.schedule;

import io.github.pavansharma36.workflow.api.WorkflowListener;
import io.github.pavansharma36.workflow.api.WorkflowManager;
import io.github.pavansharma36.workflow.api.WorkflowManagerLifecycle;
import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.bean.RunEvent;
import io.github.pavansharma36.workflow.api.bean.State;
import io.github.pavansharma36.workflow.api.bean.TaskEvent;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import io.github.pavansharma36.workflow.api.bean.task.TaskImplType;
import io.github.pavansharma36.workflow.api.bean.task.TaskType;
import io.github.pavansharma36.workflow.api.dag.RunnableTaskDag;
import io.github.pavansharma36.workflow.api.model.ManagerInfo;
import io.github.pavansharma36.workflow.api.model.RunInfo;
import io.github.pavansharma36.workflow.api.model.TaskInfo;
import io.github.pavansharma36.workflow.api.util.WorkflowException;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.pavansharma36.workflow.api.executor.ExecutableTask;
import io.github.pavansharma36.workflow.api.executor.ExecutionResult;
import io.github.pavansharma36.workflow.api.executor.TaskExecutionStatus;

/**
 * Primary class to handle all scheduling activity.
 * Scheduling tasks, updating heartbeat timestamps and maintenance.
 *
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class Scheduler implements WorkflowManagerLifecycle {

  private final AtomicReference<State> state = new AtomicReference<>(State.INIT);
  private final WorkflowAdapter adapter;

  @Override
  public void start(final WorkflowManager workflowManager) {
    if (state.compareAndSet(State.INIT, State.STARTING)) {
      startHeartbeatLoop(workflowManager, workflowManager.scheduledExecutorService());
      startHandleUpdatedRunLoop(workflowManager, workflowManager.scheduledExecutorService());
      startMaintenanceLoop(workflowManager, workflowManager.scheduledExecutorService());

      state.compareAndSet(State.STARTING, State.STARTED);
    } else {
      throw new WorkflowException("Invalid state");
    }
  }

  @Override
  public void stop() {
    if (state.compareAndSet(State.STARTED, State.STOPPING)) {
      log.info("Stopping schedulers");
      state.compareAndSet(State.STOPPING, State.STOPPED);
    } else {
      throw new WorkflowException("Invalid state");
    }
  }

  private void startMaintenanceLoop(final WorkflowManager workflowManager,
                                    final ScheduledExecutorService scheduledExecutorService) {
    boolean result = false;
    try {
      if (adapter.scheduleAdapter().isScheduler()) {
        log.info("Clearing all stuck workflows");
        Duration maxRunDuration = adapter.scheduleAdapter().maxRunDuration();
        List<RunInfo> stuckRuns = adapter.persistenceAdapter().getStuckRunInfos(maxRunDuration);
        stuckRuns.forEach(r -> {
          log.warn("Run {} stuck for more than {}, aborting now", r.getRunId(), maxRunDuration);
          workflowManager.cancelRun(r.getRunId());
        });
        result = !stuckRuns.isEmpty();

        adapter.maintenance();
      }
    } finally {
      if (state.get() != State.STOPPED) {
        final Duration duration =
            adapter.scheduleAdapter().maintenanceDelayGenerator().delay(result);
        scheduledExecutorService.schedule(() -> startMaintenanceLoop(workflowManager,
            scheduledExecutorService), duration.toMillis(), TimeUnit.MILLISECONDS);
      }
    }
  }

  private void startHeartbeatLoop(final WorkflowManager workflowManager,
                                  final ScheduledExecutorService scheduledExecutorService) {
    boolean result = false;
    try {
      ManagerInfo managerInfo = workflowManager.info();
      log.info("Updating heartbeat {}", managerInfo.getManagerId());
      managerInfo.setHeartbeatEpoch(System.currentTimeMillis());
      result = adapter.persistenceAdapter().createOrUpdateManagerInfo(managerInfo);
    } finally {
      if (state.get() != State.STOPPED) {
        final Duration duration = adapter.persistenceAdapter()
            .heartbeatDelayGenerator().delay(result);
        scheduledExecutorService.schedule(() -> startHeartbeatLoop(workflowManager,
            scheduledExecutorService), duration.toMillis(), TimeUnit.MILLISECONDS);
      }
    }
  }

  private void startHandleUpdatedRunLoop(final WorkflowManager workflowManager,
                                         final ScheduledExecutorService scheduledExecutorService) {
    boolean result = false;
    try {
      if (adapter.scheduleAdapter().isScheduler()) {
        try {
          result = handleRun(workflowManager);
        } catch (final Exception e) {
          log.error("Unknown error {}", e.getMessage(), e);
        }
      } else {
        log.debug("Not scheduler");
      }
    } finally {
      if (state.get() != State.STOPPED) {
        final Duration duration = adapter.scheduleAdapter().pollDelayGenerator().delay(result);
        scheduledExecutorService.schedule(() ->
                startHandleUpdatedRunLoop(workflowManager, scheduledExecutorService),
            duration.toMillis(),
            TimeUnit.MILLISECONDS);
      }
    }
  }

  private boolean handleRun(final WorkflowManager workflowManager) {
    final Optional<RunId> oRun = adapter.queueAdapter().pollUpdatedRun();
    if (oRun.isPresent()) {
      final RunId runId = oRun.get();
      log.info("Updating run: {}", runId);

      final Optional<RunInfo> runInfo = adapter.persistenceAdapter().getRunInfo(runId);
      if (runInfo.isPresent()) {
        updateRun(workflowManager, runId, runInfo.get());
      } else {
        adapter.queueAdapter().commitUpdatedRunProcess(runId);
      }
    }
    return oRun.isPresent();
  }

  private void updateRun(final WorkflowManager workflowManager, final RunId runId,
                         final RunInfo runInfo) {

    if (runInfo.getStartTimeEpoch() <= 0L && adapter.persistenceAdapter().updateStartTime(runId)) {
      log.info("Updated start time for run {}", runId);
      workflowManager.workflowManagerListener()
          .publishEvent(new RunEvent(runId, WorkflowListener.RunEventType.RUN_STARTED));
    }
    final Map<TaskId, TaskInfo> taskInfoCache = new HashMap<>();

    boolean completeRun = false;
    for (final RunnableTaskDag t : runInfo.getDag()) {
      final Optional<TaskInfo> taskO =
          adapter.persistenceAdapter().getTaskInfo(runId, t.getTaskId());
      if (taskO.isPresent()) {
        final TaskInfo ti = taskO.get();
        taskInfoCache.put(ti.getTaskId(), ti);
        completeRun = ti.getResult() != null
            && ti.getResult().getStatus() == TaskExecutionStatus.FAILED_STOP;
      } else {
        completeRun = true;
      }

      if (completeRun) {
        break;
      }
    }

    if (completeRun) {
      log.debug("Run has canceled tasks and will be marked completed: " + runId);
      completeRun(workflowManager, runId, false);
      return; // one or more tasks has canceled the entire run
    }

    runInfo.getDag().forEach(d -> {
      final TaskId tid = d.getTaskId();
      final TaskInfo taskInfo = taskInfoCache.get(tid);

      if (taskInfo.getTaskImplType() == TaskImplType.DECISION
          && (taskInfo.getCompletionTimeEpoch() > 0)) {
        final Collection<TaskId> childrens = d.getChildrens();
        if (childrens != null) {
          for (final TaskId taskId : childrens) {
            if (taskInfo.getResult() == null
                || !taskId.equals(taskInfo.getResult().getDecision())) {
              final TaskInfo childTask = taskInfoCache.get(taskId);
              if (childTask.getCompletionTimeEpoch() <= 0) {
                ignoreAllChildrenTasks(workflowManager, runInfo, taskId,
                    "Aborted with decision " + taskInfo.getResult().getDecision(), taskInfoCache);
              }
            }
          }
        }
      }
    });

    final Set<TaskId> completedTasks = new HashSet<>();

    runInfo.getDag().forEach(d -> {
      final TaskId tid = d.getTaskId();
      final TaskInfo taskInfo = taskInfoCache.get(tid);

      if (taskInfo.getCompletionTimeEpoch() > 0) {
        completedTasks.add(tid);
      } else if (taskInfo.getQueuedTimeEpoch() <= 0) {
        final boolean allDependenciesAreComplete = d.getDependencies().stream()
            .allMatch(t -> taskInfoCache.get(t).getCompletionTimeEpoch() > 0);
        if (allDependenciesAreComplete) {
          if (taskInfo.getType() != null) {
            queueTask(runId, tid, taskInfo.getType());
            taskInfo.setQueuedTimeEpoch(System.currentTimeMillis());
          } else {
            adapter.persistenceAdapter().completeTask(
                ExecutableTask.builder().runId(runId).taskId(tid).build(),
                ExecutionResult.builder().status(TaskExecutionStatus.SUCCESS).build());
            taskInfo.setCompletionTimeEpoch(System.currentTimeMillis());

            adapter.queueAdapter().pushUpdatedRun(runId);
          }
          if (adapter.persistenceAdapter().updateRunInfoEpoch(runId)) {
            log.debug("Updated last update epoch for run {}", runId);
          }
        }
      }
    });

    if (completedTasks
        .equals(runInfo.getDag().stream().map(RunnableTaskDag::getTaskId)
            .collect(Collectors.toSet()))) {
      completeRun(workflowManager, runId, true);
    }

    if (adapter.queueAdapter().commitUpdatedRunProcess(runId)) {
      log.debug("Commited processing of updated run");
    } else {
      log.warn("Failed to commit updated run processed {}", runId);
    }
  }

  private void ignoreAllChildrenTasks(final WorkflowManager workflowManager, final RunInfo runInfo,
                                      final TaskId taskId, final String message,
                                      final Map<TaskId, TaskInfo> taskInfoCache) {
    final Optional<RunnableTaskDag> d =
        runInfo.getDag().stream().filter(i -> i.getTaskId().equals(taskId))
            .findAny();
    if (d.isPresent() && (d.get().getChildrens() != null)) {
      d.get().getChildrens()
          .forEach(
              c -> ignoreAllChildrenTasks(workflowManager, runInfo, c, message, taskInfoCache));
    }
    final RunId runId = runInfo.getRunId();
    log.info("Ignoring task {}", taskId);

    ExecutionResult result =
        ExecutionResult.builder().message(message).status(TaskExecutionStatus.IGNORED).build();
    adapter.persistenceAdapter()
        .completeTask(ExecutableTask.builder().runId(runId).taskId(taskId).build(), result);

    workflowManager.workflowManagerListener()
        .publishEvent(new TaskEvent(runId, taskId, WorkflowListener.TaskEventType.TASK_IGNORED));
    if (taskInfoCache.containsKey(taskId)) {
      taskInfoCache.get(taskId).setCompletionTimeEpoch(System.currentTimeMillis());
    }
  }

  private void queueTask(final RunId runId, final TaskId taskId, final TaskType taskType) {
    final ExecutableTask executableTask =
        ExecutableTask.builder().runId(runId).taskId(taskId).taskType(taskType)
            .build();

    adapter.queueAdapter().pushTask(executableTask);
    if (adapter.persistenceAdapter().updateQueuedTime(runId, taskId)) {
      log.info("Updated queued time for task {} {}", runId, taskId);
    }
  }

  private void completeRun(final WorkflowManager workflowManager, final RunId runId,
                           final boolean success) {
    log.info("Completing run {}", runId);
    workflowManager.workflowManagerListener()
        .publishEvent(
            new RunEvent(runId, success ? WorkflowListener.RunEventType.RUN_COMPLETED : WorkflowListener.RunEventType.RUN_FAILED));

    adapter.persistenceAdapter().cleanup(runId);
  }

}
