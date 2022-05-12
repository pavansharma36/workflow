package org.one.workflow.api;

/**
 * Lifecycle of workflow components.
 * start/stop.
 *
 */
public interface WorkflowManagerLifecycle {

  void start(WorkflowManager workflowManager);

  void stop();

}
