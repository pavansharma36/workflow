package io.github.pavansharma36.workflow.api.bean.id;

import io.github.pavansharma36.workflow.api.util.Utils;

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
