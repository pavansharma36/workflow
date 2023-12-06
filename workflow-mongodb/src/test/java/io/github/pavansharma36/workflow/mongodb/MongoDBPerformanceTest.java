package io.github.pavansharma36.workflow.mongodb;

import io.github.pavansharma36.workflow.api.PerformanceTest;
import io.github.pavansharma36.workflow.mongodb.rule.MongoDBRule;

public class MongoDBPerformanceTest extends PerformanceTest {
  @Override
  protected MongoDBRule rule() {
    return new MongoDBRule();
  }
}
