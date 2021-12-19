package org.one.workflow.api.model;

import java.util.Map;

import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.Task;
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
	
	private String message;
	private TaskExecutionStatus status;
	private Map<String, Object> resultMeta;
	
	public TaskInfo(RunId runId, Task task) {
		this.runId = runId.getId();
		this.taskId = task.getId().getId();
		this.type = task.getType().getType();
		this.version = task.getType().getVersion();
		this.taskMeta = task.getTaskMeta();
	}
}
