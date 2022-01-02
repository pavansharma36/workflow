package org.one.workflow.api.model;

import java.util.Map;

import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.bean.task.impl.AsyncTask;
import org.one.workflow.api.bean.task.impl.DecisionTask;
import org.one.workflow.api.bean.task.impl.IdempotentTask;
import org.one.workflow.api.executor.TaskExecutionStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskInfo {
	private String runId;
	private String taskId;
	private String type;
	private int version;
	private long queuedTimeEpoch;
	private long startTimeEpoch;
	private long completionTimeEpoch;
	private Map<String, Object> taskMeta;

	private boolean async;
	private boolean idempotent;
	private boolean decision;
	private int retryCount;

	private String message;
	private TaskExecutionStatus status;
	private TaskId decisionValue;
	private Map<String, Object> resultMeta;

	public TaskInfo(final RunId runId, final Task task) {
		this.runId = runId.getId();
		this.taskId = task.getId().getId();
		if (task.getType() != null) {
			this.type = task.getType().getType();
			this.version = task.getType().getVersion();
		}
		this.taskMeta = task.getTaskMeta();

		if (task instanceof DecisionTask) {
			this.decision = true;
		}

		if (task instanceof IdempotentTask) {
			final IdempotentTask iTask = (IdempotentTask) task;
			this.idempotent = true;
			this.retryCount = iTask.getRetryCount();
		} else if (task instanceof AsyncTask) {
			this.async = true;
		}
	}
}
