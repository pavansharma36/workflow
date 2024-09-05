package io.github.pavansharma36.workflow.inmemory;

import io.github.pavansharma36.workflow.api.NormalTest;
import io.github.pavansharma36.workflow.api.junit.WorkflowTestRule;
import io.github.pavansharma36.workflow.inmemory.rule.InmemoryRule;

public class InmemoryNormalTest extends NormalTest {
  @Override
  protected WorkflowTestRule rule() {
    return new InmemoryRule();
  }
}
