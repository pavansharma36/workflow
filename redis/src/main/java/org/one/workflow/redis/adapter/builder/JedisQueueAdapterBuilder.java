package org.one.workflow.redis.adapter.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.one.workflow.api.adapter.QueueAdapter;
import org.one.workflow.api.adapter.builder.BaseAdapterBuilder;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.util.WorkflowException;
import org.one.workflow.redis.adapter.JedisQueueAdapter;
import redis.clients.jedis.JedisPool;

/**
 * Builder class for {@link org.one.workflow.api.adapter.QueueAdapter} with Redis as queue service.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JedisQueueAdapterBuilder
    extends BaseAdapterBuilder<JedisQueueAdapterBuilder, QueueAdapter> {

  private JedisPool jedis;

  public static JedisQueueAdapterBuilder builder() {
    return new JedisQueueAdapterBuilder();
  }

  public JedisQueueAdapterBuilder withJedisPool(final JedisPool jedis) {
    this.jedis = jedis;
    return this;
  }

  /**
   * Build instance of {@link org.one.workflow.api.adapter.QueueAdapter}.
   *
   * @return - instance of {@link JedisQueueAdapter}.
   */
  public JedisQueueAdapter build() {
    validate();
    if (jedis == null) {
      throw new WorkflowException("Jedis pool cant be null");
    }
    return new JedisQueueAdapter(jedis, serde, pollDelayGenerator, namespace);
  }

}
