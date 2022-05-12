package org.one.workflow.redis.adapter.builder;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.adapter.WorkflowAdapterImpl;
import org.one.workflow.api.serde.JacksonSerde;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.util.PollDelayGenerator;
import redis.clients.jedis.JedisPool;

/**
 * Builder class to build redis based {@link WorkflowAdapter}
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JedisWorkflowAdapterBuilder {

  private JedisScheduleAdapterBuilder jedisScheduleAdapterBuilder;
  private JedisPersistenceAdapterBuilder jedisPersistenceAdapterBuilder;
  private JedisQueueAdapterBuilder jedisQueueAdapterBuilder;

  public static JedisWorkflowAdapterBuilder builder(final JedisPool jedisPool,
                                                    final String namespace) {
    return builder(jedisPool, namespace, JacksonSerde.getInstance());
  }

  /**
   * Builder for jedis.
   *
   * @param jedisPool - pool to use for jedis connections.
   * @param namespace - namespace to use for this workflow.
   * @param serde - serde to use to ser/deserialize objects into redis.
   * @return - builder.
   */
  public static JedisWorkflowAdapterBuilder builder(final JedisPool jedisPool,
                                                    final String namespace,
                                                    final Serde serde) {
    final JedisWorkflowAdapterBuilder builder = new JedisWorkflowAdapterBuilder();
    builder.jedisScheduleAdapterBuilder =
        JedisScheduleAdapterBuilder.builder().withJedisPool(jedisPool)
            .withNamespace(namespace);
    builder.jedisQueueAdapterBuilder = JedisQueueAdapterBuilder.builder().withJedisPool(jedisPool)
        .withNamespace(namespace).withSerde(serde);
    builder.jedisPersistenceAdapterBuilder =
        JedisPersistenceAdapterBuilder.builder().withJedisPool(jedisPool)
            .withNamespace(namespace).withSerde(serde);
    return builder;
  }

  public JedisWorkflowAdapterBuilder withQueuePollDelayGenerator(
      final PollDelayGenerator pollDelayGenerator) {
    this.jedisQueueAdapterBuilder.withPollDelayGenerator(pollDelayGenerator);
    return this;
  }

  public JedisWorkflowAdapterBuilder withSchedulePollDelayGenerator(
      final PollDelayGenerator pollDelayGenerator) {
    this.jedisScheduleAdapterBuilder.withPollDelayGenerator(pollDelayGenerator);
    return this;
  }

  public JedisWorkflowAdapterBuilder withMaintenancePollDelayGenerator(
      final PollDelayGenerator pollDelayGenerator
  ) {
    this.jedisScheduleAdapterBuilder.withMaintenanceDelayGenerator(pollDelayGenerator);
    return this;
  }

  public JedisWorkflowAdapterBuilder withMaxRunDuration(Duration duration) {
    this.jedisScheduleAdapterBuilder.maxRunDuration(duration);
    return this;
  }

  /**
   * build {@link WorkflowAdapter}
   *
   * @return - instance of workflowadaper.
   */
  public WorkflowAdapter build() {
    return new WorkflowAdapterImpl(jedisScheduleAdapterBuilder.build(),
        jedisQueueAdapterBuilder.build(),
        jedisPersistenceAdapterBuilder.build());
  }

}
