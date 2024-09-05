package io.github.pavansharma36.workflow.inmemory;

import io.github.pavansharma36.workflow.api.PerformanceTest;
import io.github.pavansharma36.workflow.api.junit.WorkflowTestRule;
import io.github.pavansharma36.workflow.inmemory.rule.InmemoryRule;

public class InmemoryPerformanceTest extends PerformanceTest {
  @Override
  protected WorkflowTestRule rule() {
    return new InmemoryRule();
  }
}
