package org.one.workflow.api.model;

import java.util.List;

import org.one.workflow.api.RunnableTaskDag;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunInfo {
	private String runId;
	private long startTimeEpoch;
	private long completionTimeEpoch;
	private List<RunnableTaskDag> dag;
}
