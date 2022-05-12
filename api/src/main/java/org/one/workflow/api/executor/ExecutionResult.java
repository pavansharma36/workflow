package org.one.workflow.api.executor;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.one.workflow.api.bean.id.TaskId;

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
