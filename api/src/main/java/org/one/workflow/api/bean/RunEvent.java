package org.one.workflow.api.bean;

import org.one.workflow.api.WorkflowListener.RunEventType;
import org.one.workflow.api.bean.run.RunId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class RunEvent {
	private final RunId runId;
	private final RunEventType type;
}
