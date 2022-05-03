package org.one.workflow.api.bean.id;

import org.one.workflow.api.util.Utils;

/**
 * Id for workflow manager info.
 */
public class ManagerId extends Id {
  public ManagerId() {
    super(Utils.random());
  }
}
