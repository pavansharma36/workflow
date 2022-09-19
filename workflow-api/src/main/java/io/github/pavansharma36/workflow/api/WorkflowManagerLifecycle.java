package io.github.pavansharma36.workflow.api;

/**
 * Lifecycle of workflow components.
 * start/stop.
 *
 */
public interface WorkflowManagerLifecycle {

  void start(WorkflowManager workflowManager);

  void stop();

}
