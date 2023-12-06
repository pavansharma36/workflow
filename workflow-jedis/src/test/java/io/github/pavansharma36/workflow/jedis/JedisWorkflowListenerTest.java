package io.github.pavansharma36.workflow.jedis;

import io.github.pavansharma36.workflow.api.WorkflowListenerTest;
import io.github.pavansharma36.workflow.api.junit.WorkflowTestRule;
import io.github.pavansharma36.workflow.jedis.rule.JedisRule;

public class JedisWorkflowListenerTest extends WorkflowListenerTest {
  @Override
  protected WorkflowTestRule rule() {
    return new JedisRule();
  }
}
