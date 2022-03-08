package org.one.workflow.api.adapter;

import org.one.workflow.api.WorkflowManagerLifecycle;

/**
 * Api to provide implementation of {@link ScheduleAdapter}, {@link PersistenceAdapter},
 * {@link QueueAdapter}.
 */
public interface WorkflowAdapter extends WorkflowManagerLifecycle {

  /**
   * ScheduleAdapter to use for {@link org.one.workflow.api.WorkflowManager}.
   *
   * @return - {@link ScheduleAdapter} to use.
   */
  ScheduleAdapter scheduleAdapter();

  /**
   * PersistenceAdapter to use for {@link org.one.workflow.api.WorkflowManager}.
   *
   * @return - {@link PersistenceAdapter} to use.
   */
  PersistenceAdapter persistenceAdapter();

  /**
   * QueueAdapter to use for {@link org.one.workflow.api.WorkflowManager}.
   *
   * @return - {@link QueueAdapter} to use.
   */
  QueueAdapter queueAdapter();

}
