package org.one.workflow.api.bean.task.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.id.TaskId;
import org.one.workflow.api.bean.task.TaskImplType;
import org.one.workflow.api.bean.task.TaskType;

/**
 * Idempotent tasks will be retried upto given retryCount and will be continued across workflow
 * manager restarts.
 */
@Getter
public class IdempotentTask extends Task {

  private final int retryCount;

  public IdempotentTask(@NonNull final TaskType type) {
    this(type, Collections.emptyList());
  }

  public IdempotentTask(@NonNull final TaskType type, @NonNull final List<Task> childrens) {
    this(type, childrens, null);
  }

  public IdempotentTask(@NonNull final TaskType type, @NonNull final List<Task> childrens,
                        final Map<String, Object> taskMeta) {
    this(new TaskId(), type, childrens, taskMeta);
  }

  public IdempotentTask(@NonNull final TaskId id, @NonNull final TaskType type,
                        @NonNull final List<Task> childrens,
                        final Map<String, Object> taskMeta) {
    this(id, type, childrens, taskMeta, 0);
  }

  public IdempotentTask(@NonNull final TaskId id, @NonNull final TaskType type,
                        @NonNull final List<Task> childrens,
                        final Map<String, Object> taskMeta, final int retryCount) {
    super(id, type, childrens, taskMeta);
    this.retryCount = retryCount;
  }

  @Override
  public TaskImplType implType() {
    return TaskImplType.IDEMPOTENT;
  }
}
