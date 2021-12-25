package org.one.workflow.api.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.executor.TaskExecutor;
import org.one.workflow.api.impl.WorkflowManagerImpl.TaskDefination;

public class WorkflowManagerBuilder {

	private WorkflowAdapter adapter;
	private ExecutorService executorService;
	private ScheduledExecutorService scheduledExecutorService;
	private final List<TaskDefination> taskDefinations = new LinkedList<>();

	public static WorkflowManagerBuilder builder() {
		return new WorkflowManagerBuilder();
	}

	public WorkflowManagerBuilder withAdapter(final WorkflowAdapter adapter) {
		this.adapter = adapter;
		return this;
	}

	public WorkflowManagerBuilder withExecutorService(final ExecutorService executorService) {
		this.executorService = executorService;
		return this;
	}

	public WorkflowManagerBuilder withScheduledExecutorService(
			final ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
		return this;
	}

	public WorkflowManagerBuilder addingTaskExecutor(final TaskType taskType, final int threads,
			final TaskExecutor taskExecutor) {
		addingTaskExecutor(taskType, threads, taskExecutor, null);
		return this;
	}

	public WorkflowManagerBuilder addingTaskExecutor(final TaskType taskType, final int threads,
			final TaskExecutor taskExecutor, final ExecutorService executorService) {
		if (taskDefinations.stream().anyMatch(t -> t.getTaskType().equals(taskType))) {
			throw new RuntimeException("Already added executor for task type " + taskType);
		}
		taskDefinations.add(TaskDefination.builder().taskType(taskType).taskExecutor(taskExecutor).threads(threads)
				.executorService(executorService).build());
		return this;
	}

	public WorkflowManager build() {
		assert adapter != null;

		final int rootExecutorServiceThreads = taskDefinations.stream().filter(d -> d.getExecutorService() == null)
				.mapToInt(TaskDefination::getThreads).sum();

		if (executorService == null) {
			executorService = Executors.newFixedThreadPool(rootExecutorServiceThreads);
		}

		if (scheduledExecutorService == null) {
			scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		}

		return new WorkflowManagerImpl(adapter, executorService, scheduledExecutorService, taskDefinations);

	}

}
