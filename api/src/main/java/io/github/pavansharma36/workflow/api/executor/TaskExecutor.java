package io.github.pavansharma36.workflow.api.executor;

import io.github.pavansharma36.workflow.api.WorkflowManager;

/**
 * While adding task executor on building workflow manager.
 * Instance of task executor is required which will get task to process.
 */
public interface TaskExecutor {

  ExecutionResult execute(WorkflowManager manager, ExecutableTask task);

}
