package io.github.pavansharma36.workflow.inmemory.rule;

import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.junit.WorkflowTestRule;
import io.github.pavansharma36.workflow.api.util.FixedPollDelayGenerator;
import io.github.pavansharma36.workflow.inmemory.builder.InmemoryWorkflowAdapterBuilder;
import java.time.Duration;

public class InmemoryRule extends WorkflowTestRule {
  @Override
  public WorkflowAdapter  adapter() {
    return InmemoryWorkflowAdapterBuilder.builder()
        .withQueuePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofMillis(100L)))
        .withSchedulePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofMillis(100L)))
        .build();
  }


  @Override
  protected void pre() {
    // Nothing to do
  }

  @Override
  protected void post() {
    // Nothing to do
  }
}
