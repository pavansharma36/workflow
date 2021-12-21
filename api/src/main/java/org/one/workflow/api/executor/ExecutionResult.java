package org.one.workflow.api.executor;

import java.util.Map;

import org.one.workflow.api.bean.task.TaskId;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExecutionResult {
	private final TaskExecutionStatus status;
	private final String message;
	private final Map<String, Object> resultMeta;
	private final TaskId decision;
}
