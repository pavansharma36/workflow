package org.one.workflow.redis.adapter.builder;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.adapter.builder.WorkflowAdapterBuilder;
import org.one.workflow.api.adapter.impl.WorkflowAdapterImpl;
import org.one.workflow.api.serde.JacksonSerde;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.util.PollDelayGenerator;
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
