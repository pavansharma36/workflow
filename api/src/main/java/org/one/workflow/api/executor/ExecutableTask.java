package org.one.workflow.api.executor;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.one.workflow.api.bean.id.RunId;
import org.one.workflow.api.bean.id.TaskId;
import org.one.workflow.api.bean.task.TaskType;

@Getter
@Builder
@Jacksonized
public class ExecutableTask {

  private final RunId runId;
  private final TaskId taskId;
  private final TaskType taskType;
  private final Map<String, Object> taskMeta;

}
