package org.one.workflow.api.executor;

import org.one.workflow.api.WorkflowManager;

public interface TaskExecutor {

	ExecutionResult execute(WorkflowManager manager, ExecutableTask task);
	
}
