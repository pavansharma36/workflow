package org.one.workflow.api.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.executor.TaskExecutionStatus;
import org.one.workflow.api.impl.WorkflowManagerImpl.TaskDefination;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;
import org.one.workflow.api.queue.QueueConsumer;
import org.one.workflow.api.util.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueueConsumerImpl implements QueueConsumer {

	private final WorkflowAdapter adapter;
	private final Map<TaskType, TaskDefination> taskMap = new HashMap<>();
	private final Map<TaskType, AtomicInteger> inProgress = new HashMap<>();
	private final LinkedList<TaskType> taskTypeIterator;

	protected QueueConsumerImpl(final WorkflowAdapter adapter,
			final List<WorkflowManagerImpl.TaskDefination> taskDefinations) {
		this.adapter = adapter;
		taskDefinations.forEach(td -> {
			taskMap.put(td.getTaskType(), td);
		});
		taskTypeIterator = new LinkedList<>(taskMap.keySet());
	}

	@Override
	public void start(final WorkflowManager workflowManager) {
		if (!taskTypeIterator.isEmpty()) {
			run(workflowManager);
		}
	}

	private void run(final WorkflowManager workflowManager) {
		final TaskType taskType = taskTypeIterator.removeFirst();
		taskTypeIterator.addLast(taskType);

		boolean result = false;
		if (inProgress.computeIfAbsent(taskType, t -> new AtomicInteger(0)).get() < taskMap.get(taskType)
				.getThreads()) {
			final Optional<ExecutableTask> oTask = adapter.queueAdapter().pollTask(taskType);
			if (oTask.isPresent()) {
				result = true;
				inProgress.computeIfAbsent(taskType, t -> new AtomicInteger(0)).incrementAndGet();

				Objects.requireNonNullElseGet(taskMap.get(taskType).getExecutorService(),
						workflowManager::executorService).submit(() -> {
							try {
								final ExecutableTask task = oTask.get();
								final Optional<TaskInfo> oTaskInfo = adapter.persistenceAdapter()
										.getTaskInfo(task.getRunId(), task.getTaskId());
								if (oTaskInfo.isPresent()) {
									final TaskInfo taskInfo = oTaskInfo.get();
									adapter.persistenceAdapter().updateStartTime(task.getRunId(), task.getTaskId());

									ExecutionResult executionResult;

									int retry = 0;

									boolean force = false;
									do {
										try {
											if (!taskInfo.isIdempotent() && (taskInfo.getStartTimeEpoch() > 0)) {
												throw new RuntimeException(
														"Task was started previously and not idempotent");
											}

											executionResult = taskMap.get(taskType).getTaskExecutor().execute(
													workflowManager,
													ExecutableTask.builder().runId(task.getRunId())
															.taskId(task.getTaskId())
															.taskType(new TaskType(taskInfo.getVersion(),
																	taskInfo.getType()))
															.taskMeta(taskInfo.getTaskMeta()).build());

											if ((executionResult == null) || (executionResult.getStatus() == null)) {
												throw new RuntimeException("Result cannot be null");
											}

											if (taskInfo.isDecision() && ((executionResult.getDecision() == null)
													|| !validateDecision(task.getRunId(), task.getTaskId(),
															executionResult.getDecision()))) {
												throw new RuntimeException("Decision cannot be null");
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

									if (!taskInfo.isAsync() || force) {
										adapter.persistenceAdapter().completeTask(task, executionResult);

										adapter.queueAdapter().pushUpdatedRun(task.getRunId());
									}
								}

							} finally {
								inProgress.computeIfAbsent(taskType, t -> new AtomicInteger(0)).decrementAndGet();
							}
						});
			}
		}

		final Duration delay = adapter.queueAdapter().pollDelayGenerator().delay(result);
		workflowManager.scheduledExecutorService().schedule(() ->

		run(workflowManager), delay.toMillis(), TimeUnit.MILLISECONDS);
	}

	private boolean validateDecision(final RunId runId, final TaskId taskId, final TaskId decision) {
		final Optional<RunInfo> oRunInfo = adapter.persistenceAdapter().getRunInfo(runId);

		return oRunInfo.isPresent() && oRunInfo
				.map(ri -> ri.getDag().stream().filter(d -> d.getTaskId().equals(taskId)).findAny().orElse(null))
				.map(dag -> {
					if (Utils.nullSafe(dag.getChildrens()).stream().anyMatch(t -> t.equals(decision))) {
						return true;
					} else {
						return null;
					}
				}).orElseThrow(() -> new RuntimeException("Invalid runid/taskid/decision"));
	}

	@Override
	public void stop() {

	}

}
