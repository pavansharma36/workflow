package io.github.pavansharma36.workflow.mongodb;

import io.github.pavansharma36.workflow.api.WorkflowListenerTest;
import io.github.pavansharma36.workflow.api.junit.WorkflowTestRule;
import io.github.pavansharma36.workflow.mongodb.rule.MongoDBRule;

public class MongoDBWorkflowListenerTest extends WorkflowListenerTest {
  @Override
  protected WorkflowTestRule rule() {
    return new MongoDBRule();
  }
}
