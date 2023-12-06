package io.github.pavansharma36.workflow.api.executor;

import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

/**
 * Task executor needs to return result.
 */
@Getter
@Setter
public class ExecutionResult {
  private TaskExecutionStatus status;
  private String message;
  private Map<String, Object> resultMeta;
  private TaskId decision;

  public ExecutionResult() {

  }

  public ExecutionResult(TaskExecutionStatus status, String message, Map<String, Object> resultMeta,
                         TaskId decision) {
    this.status = status;
    this.message = message;
    this.resultMeta = resultMeta;
    this.decision = decision;
  }
}
