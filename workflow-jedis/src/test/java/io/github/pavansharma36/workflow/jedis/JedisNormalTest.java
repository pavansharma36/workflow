package io.github.pavansharma36.workflow.jedis;

import io.github.pavansharma36.workflow.api.NormalTest;
import io.github.pavansharma36.workflow.api.junit.WorkflowTestRule;
import io.github.pavansharma36.workflow.jedis.rule.JedisRule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JedisNormalTest extends NormalTest {
  @Override
  protected WorkflowTestRule rule() {
    return new JedisRule();
  }
}
