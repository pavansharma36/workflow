package io.github.pavansharma36.workflow.inmemory.adapter;

import io.github.pavansharma36.workflow.api.WorkflowManager;
import io.github.pavansharma36.workflow.api.adapter.ScheduleAdapter;
import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import java.time.Duration;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InmemorySchedulerAdapter implements ScheduleAdapter {

  private final PollDelayGenerator pollDelayGenerator;
  private final PollDelayGenerator maintenanceDelayGenerator;

  @Override
  public void start(WorkflowManager workflowManager) {

  }

  @Override
  public void stop() {

  }

  @Override
  public void maintenance(WorkflowAdapter workflowAdapter) {

  }

  @Override
  public PollDelayGenerator pollDelayGenerator() {
    return pollDelayGenerator;
  }

  @Override
  public PollDelayGenerator maintenanceDelayGenerator() {
    return maintenanceDelayGenerator;
  }

  @Override
  public Duration maxRunDuration() {
    return Duration.ofDays(7L);
  }

  @Override
  public boolean isScheduler() {
    return true;
  }
}
