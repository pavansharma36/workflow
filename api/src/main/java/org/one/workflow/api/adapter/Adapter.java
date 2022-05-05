package org.one.workflow.api.adapter;

import org.one.workflow.api.WorkflowManagerLifecycle;

/**
 * Adapter instance need to implement maintenance.
 */
public interface Adapter extends WorkflowManagerLifecycle {

  /**
   * maintenance can be cleaning up stale data.
   */
  void maintenance(WorkflowAdapter workflowAdapter);

}
