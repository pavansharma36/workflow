package org.one.workflow.api;

public interface WorkflowManagerLifecycle {

  void start(WorkflowManager workflowManager);

  void stop();

}
