package io.github.pavansharma36.workflow.inmemory;

import io.github.pavansharma36.workflow.api.WorkflowListenerTest;
import io.github.pavansharma36.workflow.api.junit.WorkflowTestRule;
import io.github.pavansharma36.workflow.inmemory.rule.InmemoryRule;

public class InmemoryWorkflowListenerTest extends WorkflowListenerTest {
  @Override
  protected WorkflowTestRule rule() {
    return new InmemoryRule();
  }
}
