package io.github.pavansharma36.workflow.api.bean;

import io.github.pavansharma36.workflow.api.WorkflowListener.TaskEventType;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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
