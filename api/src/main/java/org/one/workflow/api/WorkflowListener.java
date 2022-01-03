package org.one.workflow.api;

import org.one.workflow.api.bean.RunEvent;
import org.one.workflow.api.bean.TaskEvent;

public interface WorkflowListener {

	void onRunEvent(RunEvent event);

	void onTaskEvent(TaskEvent event);

	public enum RunEventType {
		RUN_STARTED, RUN_COMPLETED, RUN_FAILED, RUN_ABORTED
	}

	public enum TaskEventType {
		TASK_STARTED, TASK_COMPLETED, TASK_FAILED, TASK_IGNORED, TASK_ABORTED
	}

}
