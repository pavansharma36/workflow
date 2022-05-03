package org.one.workflow.api.schedule;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.util.PollDelayGenerator;

@Slf4j
@RequiredArgsConstructor
public class MaintenanceScheduler implements Runnable {

  private final WorkflowAdapter adapter;
  private final WorkflowManager manager;
  private final PollDelayGenerator pollDelayGenerator;

  @Override
  public void run() {
    try {

    } finally {
      Duration delay = pollDelayGenerator.delay(false);
      manager.scheduledExecutorService().schedule(this, delay.toMillis(), TimeUnit.MILLISECONDS);
    }
  }

}
