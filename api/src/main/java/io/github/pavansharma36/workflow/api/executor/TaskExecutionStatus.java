package io.github.pavansharma36.workflow.api.executor;

import io.github.pavansharma36.workflow.api.bean.task.impl.DecisionTask;

/**
 * Status of task execution.
 * <ul>
 *   <li>Success - task was successful.</li>
 *   <li>FailedContinue - task was failed but workflow execution can continue</li>
 *   <li>FailedStop - task was failed workflow exection should be stopped</li>
 *   <li>Ignored - internal status for tasks which are ignored
 *   due to {@link DecisionTask}</li>
 * </ul>
 */
public enum TaskExecutionStatus {
  SUCCESS, FAILED_CONTINUE, FAILED_STOP, IGNORED
}
