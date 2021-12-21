package org.one.workflow.api.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.executor.TaskExecutor;
import org.one.workflow.api.impl.WorkflowManagerImpl.TaskDefination;
import org.one.workflow.api.queue.QueueConsumer;
import org.one.workflow.api.queue.QueueConsumerImp;
import org.one.workflow.api.schedule.Scheduler;

public class WorkflowManagerBuilder {

	private WorkflowAdapter adapter;
	private ScheduledExecutorService scheduledExecutorService;
	private List<TaskDefination> taskDefinations = new LinkedList<>();
	
	public static WorkflowManagerBuilder builder() {
		return new WorkflowManagerBuilder();
	}

	public WorkflowManagerBuilder withAdapter(WorkflowAdapter adapter) {
		this.adapter = adapter;
		return this;
	}
	
	public WorkflowManagerBuilder addingTaskExecutor(TaskType taskType, int threads, TaskExecutor taskExecutor) {
		addingTaskExecutor(taskType, threads, taskExecutor, null);
		return this;
	}

	public WorkflowManagerBuilder addingTaskExecutor(TaskType taskType, int threads, TaskExecutor taskExecutor,
			ExecutorService executorService) {
		taskDefinations.add(TaskDefination.builder().taskType(taskType).taskExecutor(taskExecutor).threads(threads)
				.executorService(executorService).build());
		return this;
	}

	public WorkflowManager build() {
		assert adapter != null;

		int rootExecutorServiceThreads = taskDefinations.stream().filter(d -> d.getExecutorService() == null)
				.mapToInt(TaskDefination::getThreads).sum();

		ExecutorService rootExecutorService;
		if (rootExecutorServiceThreads > 0) {
			rootExecutorService = Executors.newFixedThreadPool(rootExecutorServiceThreads);
		} else {
			rootExecutorService = null;
		}

		List<QueueConsumer> consumers = new LinkedList<>();

		Scheduler scheduler = new Scheduler(
				adapter.schedulerPollDuration(), adapter.scheduleSelector(Objects
						.requireNonNullElseGet(scheduledExecutorService, () -> Executors.newScheduledThreadPool(1))),
				adapter.workflowDao());

		WorkflowManager manager = new WorkflowManagerImpl(adapter, consumers, scheduler);

		for (TaskDefination taskDefination : taskDefinations) {

			consumers.add(QueueConsumerImp.builder().dao(adapter.workflowDao()).taskType(taskDefination.getTaskType())
					.taskExecutor(taskDefination.getTaskExecutor()).workflowManager(manager)
					.threads(taskDefination.getThreads()).pollDuration(adapter.pollDuration())
					.executorService(
							Objects.requireNonNullElse(taskDefination.getExecutorService(), rootExecutorService))
					.build());

		}

		return manager;
	}

}
