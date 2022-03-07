package org.one.workflow.api.bean.task.impl;

import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.bean.task.TaskImplType;
import org.one.workflow.api.bean.task.TaskType;

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
