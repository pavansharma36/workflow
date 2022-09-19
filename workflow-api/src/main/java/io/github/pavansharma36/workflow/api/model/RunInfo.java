package io.github.pavansharma36.workflow.api.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import io.github.pavansharma36.workflow.api.bean.id.ManagerId;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.dag.RunnableTaskDag;

/**
 * RunInfo holds all details of dag run.
 */
@Getter
@Setter
public class RunInfo {
  private RunId runId;
  private ManagerId queuedBy;
  private long queuedTime;
  private long startTimeEpoch;
  private long lastUpdateEpoch;
  private List<RunnableTaskDag> dag;
}
