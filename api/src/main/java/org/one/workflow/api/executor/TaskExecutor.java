package org.one.workflow.api.executor;

import org.one.workflow.api.WorkflowManager;

/**
 * While adding task executor on building workflow manager.
 * Instance of task executor is required which will get task to process.
 */
public interface TaskExecutor {

  ExecutionResult execute(WorkflowManager manager, ExecutableTask task);

}
