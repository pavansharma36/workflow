package org.one.workflow.redis;

import java.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.one.workflow.api.WorkflowListenerTest;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.util.FixedPollDelayGenerator;
import org.one.workflow.redis.adapter.builder.JedisWorkflowAdapterBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.JedisPool;

public class RedisWorkflowListenerTest extends WorkflowListenerTest {

  @Rule
  public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis"))
      .withExposedPorts(6379);

  private JedisPool jedisPool = null;

  @Before
  public void init() {
    jedisPool = new JedisPool(redis.getHost(), redis.getFirstMappedPort());
  }

  @After
  public void tearDown() {
    jedisPool.close();
  }

  @Override
  protected WorkflowAdapter adapter() {
    final String namespace = "test";
    return JedisWorkflowAdapterBuilder.builder(jedisPool, namespace)
        .withSchedulePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofMillis(100L)))
        .withQueuePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofMillis(100L)))
        .build();
  }

}
