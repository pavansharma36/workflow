package io.github.pavansharma36.workflow.api.executor;

import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import io.github.pavansharma36.workflow.api.bean.task.TaskType;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * serializable task to push to queue.
 */
@Getter
@Builder
@Jacksonized
public class ExecutableTask {

  private final RunId runId;
  private final TaskId taskId;
  private final TaskType taskType;
  private final Map<String, Object> taskMeta;

}
