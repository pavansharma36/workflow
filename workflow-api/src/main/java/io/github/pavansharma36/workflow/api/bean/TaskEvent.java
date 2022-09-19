package io.github.pavansharma36.workflow.api.bean;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import io.github.pavansharma36.workflow.api.WorkflowListener.TaskEventType;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;

/**
 * Event published for given {@link TaskEventType}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class TaskEvent {
  private final RunId runId;
  private final TaskId taskId;
  private final TaskEventType type;
}
