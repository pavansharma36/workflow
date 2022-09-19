package io.github.pavansharma36.workflow.api.adapter;

import io.github.pavansharma36.workflow.api.WorkflowManagerLifecycle;

/**
 * Adapter instance need to implement maintenance.
 */
public interface Adapter extends WorkflowManagerLifecycle {

  /**
   * maintenance can be cleaning up stale data.
   */
  void maintenance(WorkflowAdapter workflowAdapter);

}
