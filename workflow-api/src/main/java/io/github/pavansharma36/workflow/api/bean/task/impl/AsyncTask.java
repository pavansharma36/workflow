package io.github.pavansharma36.workflow.api.bean.task.impl;

import io.github.pavansharma36.workflow.api.WorkflowManager;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import io.github.pavansharma36.workflow.api.bean.task.Task;
import io.github.pavansharma36.workflow.api.bean.task.TaskImplType;
import io.github.pavansharma36.workflow.api.bean.task.TaskType;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Async task which is not marked completed by workflow.
 * Until completed by {@link WorkflowManager}.completeAsyncTask.
 */
public class AsyncTask extends Task {

  public AsyncTask(@NonNull final TaskType type) {
    super(type);
  }

  public AsyncTask(@NonNull final TaskType type, @NonNull final List<Task> childrens) {
    super(type, childrens);
  }

  public AsyncTask(@NonNull final TaskType type, @NonNull final List<Task> childrens,
                   final Map<String, Object> taskMeta) {
    super(new TaskId(), type, childrens, taskMeta);
  }

  public AsyncTask(@NonNull final TaskId id, @NonNull final TaskType type,
                   @NonNull final List<Task> childrens,
                   final Map<String, Object> taskMeta) {
    super(id, type, childrens, taskMeta);
  }

  @Override
  public TaskImplType implType() {
    return TaskImplType.ASYNC;
  }
}
