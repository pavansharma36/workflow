package io.github.pavansharma36.workflow.api;

import io.github.pavansharma36.workflow.api.bean.RunEvent;
import io.github.pavansharma36.workflow.api.bean.TaskEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Workflow manager lister holds all listener and publishes events.
 * In clustered workflow manager only one node will receive given event.
 * For run level event scheduler node will receive event.
 * for task level event node which processes task will receive event.
 */
@Slf4j
public class WorkflowManagerListener {

  private final List<WorkflowListener> listeners = new LinkedList<>(
      Collections.singleton(new LoggerWorkflowListener()));

  public void addListener(final WorkflowListener listener) {
    this.listeners.add(listener);
  }

  /**
   * publish run event to all {@link WorkflowListener}.
   *
   * @param event - event.
   */
  public void publishEvent(final RunEvent event) {
    listeners.forEach(l -> {
      try {
        l.onRunEvent(event);
      } catch (final Exception e) {
        log.error("Unhandled error onRunEvent {}", e.getMessage(), e);
      }
    });
  }

  /**
   * publish task event to all listeners.
   *
   * @param event - event.
   */
  public void publishEvent(final TaskEvent event) {
    listeners.forEach(l -> {
      try {
        l.onTaskEvent(event);
      } catch (final Exception e) {
        log.error("Unhandled error onTaskEvent {} {}", event, e.getMessage(), e);
      }
    });
  }

  @Slf4j
  private static final class LoggerWorkflowListener implements WorkflowListener {

    @Override
    public void onRunEvent(final RunEvent event) {
      log.info("Run event {}", event);
    }

    @Override
    public void onTaskEvent(final TaskEvent event) {
      log.info("Task event {}", event);
    }

  }

}
