package io.github.pavansharma36.workflow.api.adapter;

import io.github.pavansharma36.workflow.api.WorkflowManager;
import io.github.pavansharma36.workflow.api.WorkflowManagerLifecycle;

/**
 * Api to provide implementation of {@link ScheduleAdapter}, {@link PersistenceAdapter},
 * {@link QueueAdapter}.
 */
public interface WorkflowAdapter extends WorkflowManagerLifecycle {

  /**
   * ScheduleAdapter to use for {@link WorkflowManager}.
   *
   * @return - {@link ScheduleAdapter} to use.
   */
  ScheduleAdapter scheduleAdapter();

  /**
   * PersistenceAdapter to use for {@link WorkflowManager}.
   *
   * @return - {@link PersistenceAdapter} to use.
   */
  PersistenceAdapter persistenceAdapter();

  /**
   * QueueAdapter to use for {@link WorkflowManager}.
   *
   * @return - {@link QueueAdapter} to use.
   */
  QueueAdapter queueAdapter();

  void maintenance();

}
