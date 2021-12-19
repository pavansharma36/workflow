package org.one.workflow.api.core;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import org.one.workflow.api.queue.WorkflowDao;
import org.one.workflow.api.schedule.ScheduleSelector;

public interface WorkflowAdapter {
	
	void preStart();
	
	void postStart();
	
	WorkflowDao workflowDao();
	
	Duration pollDuration();
	
	Duration schedulerPollDuration();
	
	ScheduleSelector scheduleSelector(ScheduledExecutorService executorService);
	
	void preClose();
	
	void postClose();

}
