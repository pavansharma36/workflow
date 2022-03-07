package org.one.workflow.api.util;

/**
 * Generic runtime exception if any error in workflow execution.
 */
public class WorkflowException extends RuntimeException {
  public WorkflowException(String message) {
    this(message, null);
  }

  public WorkflowException(String message, Throwable cause) {
    super(message, cause);
  }
}
