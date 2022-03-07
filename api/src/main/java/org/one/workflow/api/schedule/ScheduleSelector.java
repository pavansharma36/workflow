package org.one.workflow.api.schedule;

public interface ScheduleSelector {

  void start();

  boolean isScheduler();

  void stop();

}
