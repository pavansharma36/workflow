package org.one.workflow.api.queue;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.executor.TaskExecutionStatus;
import org.one.workflow.api.executor.TaskExecutor;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class QueueConsumerImp implements QueueConsumer {

	private final AtomicInteger inProgressCount = new AtomicInteger(0);
	private final TaskType taskType;
	private final TaskExecutor taskExecutor;
	private final WorkflowManager workflowManager;
	private final int threads;
	private final Duration pollDuration;
	private final ExecutorService executorService;
	private final WorkflowDao dao;
	
	@Override
	public void close() {
		
	}

	@Override
	public void run() {
		while(!Thread.interrupted()) {
			try {
				if(inProgressCount.get() < threads) {
					Optional<ExecutableTask> oTask = dao.pollTask(taskType);
					if(oTask.isPresent()) {
						inProgressCount.incrementAndGet();
						executorService.submit(new Runnable() {
							@Override
							public void run() {
								try {
									ExecutableTask task = oTask.get();
									
									dao.updateStartTime(task.getRunId(), task.getTaskId());
									
									ExecutionResult result;
									try {
										result = taskExecutor.execute(workflowManager, task);
									} catch (Throwable e) {
										log.error("Unhandled error in task execution {}", e.getMessage(), e);
										result = ExecutionResult.builder()
												.status(TaskExecutionStatus.FAILED_STOP)
												.message(e.getMessage())
												.build();
									}
									
									dao.completeTask(task.getRunId(), task.getTaskId(), result);
									
									dao.pushUpdatedRun(task.getRunId());
								} finally {
									inProgressCount.decrementAndGet();
								}
							}
						});
					}
				}
			} finally {
				try {
					Thread.sleep(pollDuration.toMillis());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

}
