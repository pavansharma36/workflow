package io.github.pavansharma36.workflow.api.bean.task.impl;

import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import io.github.pavansharma36.workflow.api.bean.task.Task;
import io.github.pavansharma36.workflow.api.bean.task.TaskImplType;
import io.github.pavansharma36.workflow.api.bean.task.TaskType;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Simple implementation of {@link Task}.
 */
public class SimpleTask extends Task {

  public SimpleTask(@NonNull final TaskType type) {
    super(type);
  }

  public SimpleTask(@NonNull final TaskType type, @NonNull final List<Task> childrens) {
    super(type, childrens);
  }

  public SimpleTask(@NonNull final TaskType type, @NonNull final List<Task> childrens,
                    final Map<String, Object> taskMeta) {
    super(new TaskId(), type, childrens, taskMeta);
  }

  public SimpleTask(@NonNull TaskId id, TaskType type,
                    @NonNull List<Task> childrens) {
    super(id, type, childrens);
  }

  public SimpleTask(@NonNull final TaskId id, @NonNull final TaskType type,
                    @NonNull final List<Task> childrens,
                    final Map<String, Object> taskMeta) {
    super(id, type, childrens, taskMeta);
  }

  @Override
  public TaskImplType implType() {
    return TaskImplType.SIMPLE;
  }
}
