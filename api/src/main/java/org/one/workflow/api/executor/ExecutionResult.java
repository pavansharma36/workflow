package org.one.workflow.api.executor;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExecutionResult {
	private final TaskExecutionStatus status;
	private final String message;
	private final Map<String, Object> resultMeta;
}
