package org.one.workflow.api.bean;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.one.workflow.api.WorkflowListener.TaskEventType;
import org.one.workflow.api.bean.id.RunId;
import org.one.workflow.api.bean.id.TaskId;

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
