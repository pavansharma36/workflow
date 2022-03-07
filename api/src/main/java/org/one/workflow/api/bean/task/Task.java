package org.one.workflow.api.bean.task;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public abstract class Task {

  private final @NonNull TaskId id;
  private final TaskType type;
  private final @NonNull List<Task> childrens;
  private final Map<String, Object> taskMeta;

  public Task(final TaskType type) {
    this(new TaskId(), type, Collections.emptyList(), null);
  }

  public Task(final TaskType type, @NonNull final List<Task> childrens) {
    this(new TaskId(), type, childrens, null);
  }

}
