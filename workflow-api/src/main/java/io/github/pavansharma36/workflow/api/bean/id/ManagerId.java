package io.github.pavansharma36.workflow.api.bean.id;

import io.github.pavansharma36.workflow.api.util.Utils;

/**
 * Id for workflow manager info.
 */
public class ManagerId extends Id {
  public ManagerId() {
    super(Utils.random());
  }

  public ManagerId(String id) {
    super(id);
  }
}
