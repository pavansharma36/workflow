package io.github.pavansharma36.workflow.jedis.rule;

import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.junit.WorkflowTestRule;
import io.github.pavansharma36.workflow.api.util.FixedPollDelayGenerator;
import io.github.pavansharma36.workflow.jedis.adapter.builder.JedisWorkflowAdapterBuilder;
import java.time.Duration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.JedisPool;

public class JedisRule extends WorkflowTestRule {

  private GenericContainer redis = new GenericContainer(DockerImageName.parse("redis"))
      .withExposedPorts(6379);
  private JedisPool jedisPool = null;

  @Override
  public WorkflowAdapter adapter() {
    final String namespace = "test";
    //final JedisPool jedisPool = new JedisPool(redis.getHost(), redis.getFirstMappedPort());
    return JedisWorkflowAdapterBuilder.builder(jedisPool, namespace)
        .withSchedulePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofMillis(100L)))
        .withQueuePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofMillis(100L)))
        .build();
  }

  @Override
  protected void pre() {
    redis.start();
    jedisPool = new JedisPool(redis.getHost(), redis.getFirstMappedPort());
  }

  @Override
  protected void post() {
    jedisPool.close();
    redis.stop();
  }

}
