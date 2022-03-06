package org.one.workflow.api.model;

import java.util.List;

import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.dag.RunnableTaskDag;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunInfo {
	private RunId runId;
	private long queuedTime;
	private long startTimeEpoch;
	private long lastUpdateEpoch;
	private List<RunnableTaskDag> dag;
}
