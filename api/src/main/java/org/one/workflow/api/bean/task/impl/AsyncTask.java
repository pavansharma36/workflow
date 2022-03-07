package org.one.workflow.api.bean.task.impl;

import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.bean.task.TaskType;

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

}
