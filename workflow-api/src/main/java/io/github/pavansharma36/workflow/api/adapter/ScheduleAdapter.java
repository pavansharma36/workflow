package io.github.pavansharma36.workflow.api.adapter;

import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import java.time.Duration;

/**
 * Api to identify scheduler.
 */
public interface ScheduleAdapter extends Adapter {

  /**
   * {@link PollDelayGenerator} for polling queue.
   *
   * @return - {@link PollDelayGenerator}
   */
  PollDelayGenerator pollDelayGenerator();

  PollDelayGenerator maintenanceDelayGenerator();

  Duration maxRunDuration();

  /**
   * To identify scheduler.
   *
   * @return - if scheduler or not
   */
  boolean isScheduler();

}
