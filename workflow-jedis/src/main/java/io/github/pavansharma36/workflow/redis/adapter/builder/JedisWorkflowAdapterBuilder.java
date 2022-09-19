package io.github.pavansharma36.workflow.redis.adapter.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.WorkflowAdapterBuilder;
import io.github.pavansharma36.workflow.api.adapter.impl.WorkflowAdapterImpl;
import io.github.pavansharma36.workflow.api.serde.JacksonSerde;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import redis.clients.jedis.JedisPool;

/**
 * Builder class to build redis based {@link WorkflowAdapter}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JedisWorkflowAdapterBuilder extends WorkflowAdapterBuilder<JedisWorkflowAdapterBuilder,
    JedisScheduleAdapterBuilder, JedisPersistenceAdapterBuilder, JedisQueueAdapterBuilder> {

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
    builder.scheduleAdapterBuilder =
        JedisScheduleAdapterBuilder.builder().withJedisPool(jedisPool)
            .withNamespace(namespace);
    builder.queueAdapterBuilder = JedisQueueAdapterBuilder.builder().withJedisPool(jedisPool)
        .withNamespace(namespace).withSerde(serde);
    builder.persistenceAdapterBuilder =
        JedisPersistenceAdapterBuilder.builder().withJedisPool(jedisPool)
            .withNamespace(namespace).withSerde(serde);
    return builder;
  }

}
