package io.github.pavansharma36.workflow.jedis;

import io.github.pavansharma36.workflow.api.PerformanceTest;
import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.util.FixedPollDelayGenerator;
import io.github.pavansharma36.workflow.jedis.adapter.builder.JedisWorkflowAdapterBuilder;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.JedisPool;

@Slf4j
public class JedisPerformanceTest extends PerformanceTest {
  @Rule
  public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis"))
      .withExposedPorts(6379);

  private JedisPool jedisPool = null;

  @Before
  public void init() {
    log.warn("Initializing jedis pool");
    jedisPool = new JedisPool(redis.getHost(), redis.getFirstMappedPort());
  }

  @After
  public void tearDown() {
    log.warn("Closing jedis pool");
    jedisPool.close();
  }

  @Override
  protected WorkflowAdapter adapter() {
    final String namespace = "test";
    //final JedisPool jedisPool = new JedisPool(redis.getHost(), redis.getFirstMappedPort());
    return JedisWorkflowAdapterBuilder.builder(jedisPool, namespace)
        .withSchedulePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofMillis(10L)))
        .withQueuePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofMillis(10L)))
        .build();
  }
}
