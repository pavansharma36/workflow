package io.github.pavansharma36.workflow.api.model;

import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.github.pavansharma36.workflow.api.bean.id.ManagerId;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import io.github.pavansharma36.workflow.api.bean.task.Task;
import io.github.pavansharma36.workflow.api.bean.task.TaskImplType;
import io.github.pavansharma36.workflow.api.bean.task.TaskType;
import io.github.pavansharma36.workflow.api.bean.task.impl.IdempotentTask;
import io.github.pavansharma36.workflow.api.executor.ExecutionResult;

/**
 * TaskInfo holds all details of task in dagrun.
 */
@Getter
@Setter
@NoArgsConstructor
public class TaskInfo {
  private RunId runId;
  private TaskId taskId;
  private ManagerId processedBy;
  private TaskType type;
  private long queuedTimeEpoch;
  private long startTimeEpoch;
  private long completionTimeEpoch;
  private Map<String, Object> taskMeta;

  private TaskImplType taskImplType;
  private int retryCount;

  private ExecutionResult result;

  /**
   * Creates Task info from given runId and task.
   *
   * @param runId - id of dag run
   * @param task - task to process.
   */
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
