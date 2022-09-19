package io.github.pavansharma36.workflow.api.bean.task;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;

/**
 * Base class for all task implementations.
 */
@Getter
@ToString
@AllArgsConstructor
public abstract class Task {

  private final @NonNull TaskId id;
  private final TaskType type;
  private final @NonNull List<Task> childrens;
  private final Map<String, Object> taskMeta;

  protected Task(final TaskType type) {
    this(new TaskId(), type, Collections.emptyList(), null);
  }

  protected Task(@NonNull TaskId id, TaskType type,
              @NonNull List<Task> childrens) {
    this(id, type, childrens, null);
  }

  protected Task(final TaskType type, @NonNull final List<Task> childrens) {
    this(new TaskId(), type, childrens, null);
  }

  public abstract TaskImplType implType();

}
