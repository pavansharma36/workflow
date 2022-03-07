package org.one.workflow.api.bean.task;

import org.one.workflow.api.bean.Id;
import org.one.workflow.api.util.Utils;

public class TaskId extends Id {

  public TaskId() {
    this(Utils.random());
  }

  public TaskId(String id) {
    super(id);
  }

}
