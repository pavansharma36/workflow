package org.one.workflow.api.schedule;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.one.workflow.api.WorkflowListener.RunEventType;
import org.one.workflow.api.WorkflowListener.TaskEventType;
import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.WorkflowManagerLifecycle;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.bean.RunEvent;
import org.one.workflow.api.bean.TaskEvent;
import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.dag.RunnableTaskDag;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.executor.TaskExecutionStatus;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public class Scheduler implements WorkflowManagerLifecycle {

	private final WorkflowAdapter adapter;

	@Override
	public void start(final WorkflowManager workflowManager) {
		run(workflowManager, workflowManager.scheduledExecutorService());
	}

	@Override
	public void stop() {
		log.info("Stopping schedulers");
	}

	public void run(final WorkflowManager workflowManager, final ScheduledExecutorService scheduledExecutorService) {
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
			final Duration duration = adapter.scheduleAdapter().pollDelayGenerator().delay(result);
			scheduledExecutorService.schedule(() -> run(workflowManager, scheduledExecutorService), duration.toMillis(),
					TimeUnit.MILLISECONDS);
		}
	}

	private boolean handleRun(final WorkflowManager workflowManager) {
		final Optional<RunId> oRun = adapter.queueAdapter().pollUpdatedRun();
		if (oRun.isPresent()) {
			final RunId runId = oRun.get();
			log.info("Updating run: {}", runId);

			final Optional<RunInfo> runInfo = adapter.persistenceAdapter().getRunInfo(runId);
			runInfo.ifPresent(info -> updateRun(workflowManager, runId, info));
		}
		return oRun.isPresent();
	}

	private void updateRun(final WorkflowManager workflowManager, final RunId runId, final RunInfo runInfo) {

		if (runInfo.getStartTimeEpoch() <= 0L) {
			adapter.persistenceAdapter().updateStartTime(runId);

			workflowManager.workflowManagerListener().publishEvent(new RunEvent(runId, RunEventType.RUN_STARTED));
		}

		final Map<TaskId, TaskInfo> taskInfoCache = new HashMap<>();

		boolean completeRun = false;
		for (final RunnableTaskDag t : runInfo.getDag()) {
			final Optional<TaskInfo> taskO = adapter.persistenceAdapter().getTaskInfo(runId, t.getTaskId());
			if (taskO.isPresent()) {
				final TaskInfo ti = taskO.get();
				taskInfoCache.put(ti.getTaskId(), ti);
				completeRun = ti.getStatus() == TaskExecutionStatus.FAILED_STOP;
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

			if (taskInfo.isDecision() && (taskInfo.getCompletionTimeEpoch() > 0)) {
				final Collection<TaskId> childrens = d.getChildrens();
				if (childrens != null) {
					for (final TaskId taskId : childrens) {
						if (!taskId.equals(taskInfo.getDecisionValue())) {
							final TaskInfo childTask = taskInfoCache.get(taskId);
							if (childTask.getCompletionTimeEpoch() <= 0) {
								ignoreAllChildrenTasks(workflowManager, runInfo, taskId,
										"Aborted with decision " + taskInfo.getDecisionValue(), taskInfoCache);
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
				}
			}
		});

		if (completedTasks
				.equals(runInfo.getDag().stream().map(RunnableTaskDag::getTaskId).collect(Collectors.toSet()))) {
			completeRun(workflowManager, runId, true);
		}
	}

	private void ignoreAllChildrenTasks(final WorkflowManager workflowManager, final RunInfo runInfo,
			final TaskId taskId, final String message, final Map<TaskId, TaskInfo> taskInfoCache) {
		final Optional<RunnableTaskDag> d = runInfo.getDag().stream().filter(i -> i.getTaskId().equals(taskId))
				.findAny();
		if (d.isPresent() && (d.get().getChildrens() != null)) {
			d.get().getChildrens()
					.forEach(c -> ignoreAllChildrenTasks(workflowManager, runInfo, c, message, taskInfoCache));
		}
		final RunId runId = new RunId(runInfo.getRunId());
		log.info("Ignoring task {}", taskId);

		ExecutionResult result = ExecutionResult.builder().message(message).status(TaskExecutionStatus.IGNORED).build();
		adapter.persistenceAdapter().completeTask(ExecutableTask.builder().runId(runId).taskId(taskId).build(), result);

		workflowManager.workflowManagerListener()
				.publishEvent(new TaskEvent(runId, taskId, TaskEventType.TASK_IGNORED));
		if (taskInfoCache.containsKey(taskId)) {
			taskInfoCache.get(taskId).setCompletionTimeEpoch(System.currentTimeMillis());
		}
	}

	private void queueTask(final RunId runId, final TaskId taskId, final TaskType taskType) {
		final ExecutableTask executableTask = ExecutableTask.builder().runId(runId).taskId(taskId).taskType(taskType)
				.build();

		adapter.queueAdapter().pushTask(executableTask);
		adapter.persistenceAdapter().updateQueuedTime(runId, taskId);
	}

	private void completeRun(final WorkflowManager workflowManager, final RunId runId, final boolean success) {
		log.info("Completing run {}", runId);
		adapter.persistenceAdapter().cleanup(runId);

		workflowManager.workflowManagerListener()
				.publishEvent(new RunEvent(runId, success ? RunEventType.RUN_COMPLETED : RunEventType.RUN_FAILED));
	}

}
