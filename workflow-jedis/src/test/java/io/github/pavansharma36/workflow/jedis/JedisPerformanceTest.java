package io.github.pavansharma36.workflow.jedis;

import io.github.pavansharma36.workflow.api.PerformanceTest;
import io.github.pavansharma36.workflow.api.junit.WorkflowTestRule;
import io.github.pavansharma36.workflow.jedis.rule.JedisRule;

public class JedisPerformanceTest extends PerformanceTest {

  @Override
  protected WorkflowTestRule rule() {
    return new JedisRule();
  }
}
