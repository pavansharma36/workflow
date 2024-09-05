package io.github.pavansharma36.workflow.api.bean.task.impl;

import io.github.pavansharma36.workflow.api.bean.task.Task;
import io.github.pavansharma36.workflow.api.bean.task.TaskImplType;
import java.util.List;

/**
 * Task which doesnt have any type. Used as container task (eg. Start of Dag)
 */
public class RootTask extends Task {

  public RootTask(final List<Task> childrens) {
    super(null, childrens);
  }

  @Override
  public TaskImplType implType() {
    return TaskImplType.ROOT;
  }
}
