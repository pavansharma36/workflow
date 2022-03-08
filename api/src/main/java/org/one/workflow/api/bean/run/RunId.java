package org.one.workflow.api.bean.run;

import org.one.workflow.api.bean.Id;
import org.one.workflow.api.util.Utils;

/**
 * Wrapper class for id of submitted runs.
 */
public class RunId extends Id {

  public RunId() {
    this(Utils.random());
  }

  public RunId(String id) {
    super(id);
  }

}
