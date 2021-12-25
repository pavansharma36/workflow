package org.one.workflow.api.adapter;

import org.one.workflow.api.WorkflowManager;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WorkflowAdapterImpl implements WorkflowAdapter {

	private final ScheduleAdapter scheduleAdapter;
	private final QueueAdapter queueAdapter;
	private final PersistenceAdapter persistenceAdapter;

	@Override
	public void start(final WorkflowManager workflowManager) {
		persistenceAdapter.start(workflowManager);
		scheduleAdapter.start(workflowManager);
		queueAdapter.start(workflowManager);
	}

	@Override
	public void stop() {
		queueAdapter.stop();
		scheduleAdapter.stop();
		persistenceAdapter.stop();
	}

	@Override
	public ScheduleAdapter scheduleAdapter() {
		return scheduleAdapter;
	}

	@Override
	public PersistenceAdapter persistenceAdapter() {
		return persistenceAdapter;
	}

	@Override
	public QueueAdapter queueAdapter() {
		return queueAdapter;
	}

}
