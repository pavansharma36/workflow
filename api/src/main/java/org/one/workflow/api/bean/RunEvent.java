package org.one.workflow.api.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.one.workflow.api.WorkflowListener.RunEventType;
import org.one.workflow.api.bean.run.RunId;

@Getter
@AllArgsConstructor
@ToString
public class RunEvent {
  private final RunId runId;
  private final RunEventType type;
}
