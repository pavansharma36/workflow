package org.one.workflow.redis;

import org.junit.Before;
import org.junit.Rule;
import org.one.workflow.api.NormalTest;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.redis.adapter.builder.JedisWorkflowAdapterBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.JedisPool;

public class RedisNormalTest extends NormalTest {

  @Rule
  public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis"))
      .withExposedPorts(6379);

  @Override
  protected WorkflowAdapter adapter() {
    final JedisPool jedisPool = new JedisPool(redis.getHost(), redis.getFirstMappedPort());
    final String namespace = "test";
    return JedisWorkflowAdapterBuilder.builder(jedisPool, namespace).build();
  }
}
