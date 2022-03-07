package org.one.workflow.api.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.one.workflow.api.WorkflowListener.TaskEventType;
import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.TaskId;

@Getter
@ToString
@AllArgsConstructor
public class TaskEvent {
  private final RunId runId;
  private final TaskId taskId;
  private final TaskEventType type;
}
