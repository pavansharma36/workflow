package io.github.pavansharma36.workflow.redis.adapter.builder;

import io.github.pavansharma36.workflow.redis.adapter.JedisQueueAdapter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import io.github.pavansharma36.workflow.api.adapter.QueueAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.BaseAdapterBuilder;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.api.util.WorkflowException;
import redis.clients.jedis.JedisPool;

/**
 * Builder class for {@link io.github.pavansharma36.workflow.api.adapter.QueueAdapter} with Redis as queue service.
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
   * Build instance of {@link io.github.pavansharma36.workflow.api.adapter.QueueAdapter}.
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
