package io.github.pavansharma36.workflow.api.bean.id;

import io.github.pavansharma36.workflow.api.util.Utils;

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
