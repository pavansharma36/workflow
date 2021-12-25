package org.one.workflow.api.adapter;

import org.one.workflow.api.WorkflowManagerLifecycle;
import org.one.workflow.api.util.PollDelayGenerator;

public interface ScheduleAdapter extends WorkflowManagerLifecycle {

	PollDelayGenerator pollDelayGenerator();

	boolean isScheduler();

}
