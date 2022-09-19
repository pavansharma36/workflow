package io.github.pavansharma36.workflow.api.executor;

import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Task executor needs to return result.
 */
@Getter
@Builder
@Jacksonized
public class ExecutionResult {
  private final TaskExecutionStatus status;
  private final String message;
  private final Map<String, Object> resultMeta;
  private final TaskId decision;
}
