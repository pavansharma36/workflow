package org.one.workflow.api.model;

import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.bean.task.TaskImplType;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.bean.task.impl.AsyncTask;
import org.one.workflow.api.bean.task.impl.DecisionTask;
import org.one.workflow.api.bean.task.impl.IdempotentTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.executor.TaskExecutionStatus;

@Getter
@Setter
@NoArgsConstructor
public class TaskInfo {
  private RunId runId;
  private TaskId taskId;
  private TaskType type;
  private long queuedTimeEpoch;
  private long startTimeEpoch;
  private long completionTimeEpoch;
  private Map<String, Object> taskMeta;

  private TaskImplType taskImplType;
  private int retryCount;

  private ExecutionResult result;

  public TaskInfo(final RunId runId, final Task task) {
    this.runId = runId;
    this.taskId = task.getId();
    this.type = task.getType();
    this.taskMeta = task.getTaskMeta();

    this.taskImplType = task.implType();

    if (task instanceof IdempotentTask) {
      final IdempotentTask iTask = (IdempotentTask) task;
      this.retryCount = iTask.getRetryCount();
    }
  }
}
