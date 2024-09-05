package io.github.pavansharma36.workflow.api.bean;

import io.github.pavansharma36.workflow.api.WorkflowListener.RunEventType;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Event published for given {@link RunEventType}.
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RunEvent {
  private final RunId runId;
  private final RunEventType type;
}
