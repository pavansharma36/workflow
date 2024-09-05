package io.github.pavansharma36.workflow.mongodb;

import io.github.pavansharma36.workflow.api.NormalTest;
import io.github.pavansharma36.workflow.mongodb.rule.MongoDBRule;

public class MongoDBNormalTest extends NormalTest {

  @Override
  protected MongoDBRule rule() {
    return new MongoDBRule();
  }
}
