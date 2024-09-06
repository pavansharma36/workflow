package io.github.pavansharma36.workflow.api.executor;

import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import io.github.pavansharma36.workflow.api.bean.task.TaskType;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * serializable task to push to queue.
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class ExecutableTask {

  private RunId runId;
  private TaskId taskId;
  private TaskType taskType;
  private Map<String, Object> taskMeta;

}
