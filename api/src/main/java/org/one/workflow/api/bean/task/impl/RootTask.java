package org.one.workflow.api.bean.task.impl;

import java.util.List;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskImplType;

public class RootTask extends Task {

  public RootTask(final List<Task> childrens) {
    super(null, childrens);
  }

  @Override
  public TaskImplType implType() {
    return TaskImplType.ROOT;
  }
}
