package org.one.workflow.redis;

import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.TaskType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WorkflowRedisKeyNamesCreator {
	
	public static final String RUN_INFO_KEY = "runinfo";
	
	public static final String QUEUED_TASK_KEY_PREFIX = "taskqueue";
	public static final String QUEUED_TASK_CHECK_KEY_PREFIX = "taskcheckqueue";
	
	public static final String UPDATED_RUN_QUEUE = "updatedrun";
	public static final String UPDATED_RUN_CHECK_QUEUE = "updatedruncheck";
	
	private final String namespace;
	
	public String getLeaderElectionKey() {
		return String.format("%s::leader", namespace);
	}
	
	public String getRunInfoKey() {
		return String.format("%s::%s", namespace, RUN_INFO_KEY);
	}
	
	public String getTaskInfoKey(RunId runId) {
		return String.format("%s::taskinfo::%s", namespace, runId.getId());
	}
	
	public String getQueuedTaskKey(TaskType taskType) {
		return String.format("%s::%s::%s::%s", namespace, QUEUED_TASK_KEY_PREFIX, taskType.getType(), taskType.getVersion());
	}
	
	public String getQueuedTaskCheckKey() {
		return String.format("%s::%s", namespace, QUEUED_TASK_CHECK_KEY_PREFIX);
	}
	
	public String getUpdatedRunQueue() {
		return String.format("%s::%s", namespace, UPDATED_RUN_QUEUE);
	}
	
	public String getUpdatedRunQueueCheck() {
		return String.format("%s::%s", namespace, UPDATED_RUN_CHECK_QUEUE);
	}
	
}
