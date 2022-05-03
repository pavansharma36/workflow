package org.one.workflow.api.bean.id;

import org.one.workflow.api.bean.id.Id;
import org.one.workflow.api.util.Utils;

/**
 * Wrapper class for Id of task submitted.
 */
public class TaskId extends Id {

  public TaskId() {
    this(Utils.random());
  }

  public TaskId(String id) {
    super(id);
  }

}
