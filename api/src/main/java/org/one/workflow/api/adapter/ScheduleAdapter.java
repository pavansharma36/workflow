package org.one.workflow.api.adapter;

import org.one.workflow.api.WorkflowManagerLifecycle;
import org.one.workflow.api.util.PollDelayGenerator;

/**
 * Api to identify scheduler
 */
public interface ScheduleAdapter extends WorkflowManagerLifecycle {

  /**
   * {@link PollDelayGenerator} for polling queue.
   *
   * @return - {@link PollDelayGenerator}
   */
  PollDelayGenerator pollDelayGenerator();

  /**
   * To identify scheduler.
   *
   * @return - if scheduler or not
   */
  boolean isScheduler();

}
