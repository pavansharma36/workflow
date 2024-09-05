package io.github.pavansharma36.workflow.api;

import io.github.pavansharma36.workflow.api.bean.RunEvent;
import io.github.pavansharma36.workflow.api.bean.TaskEvent;

/**
 * Listener instance to capture events.
 */
public interface WorkflowListener {

  void onRunEvent(RunEvent event);

  void onTaskEvent(TaskEvent event);

  /**
   * event type for dag run events.
   */
  enum RunEventType {
    RUN_STARTED, RUN_COMPLETED, RUN_FAILED, RUN_ABORTED
  }

  /**
   * event type for task events.
   */
  enum TaskEventType {
    TASK_STARTED, TASK_COMPLETED, TASK_FAILED, TASK_IGNORED
  }

}
