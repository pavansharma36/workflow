package org.one.workflow.api.adapter;

import org.one.workflow.api.WorkflowManagerLifecycle;

public interface WorkflowAdapter extends WorkflowManagerLifecycle {

	ScheduleAdapter scheduleAdapter();

	PersistenceAdapter persistenceAdapter();

	QueueAdapter queueAdapter();

}
