package org.one.workflow.redis.adapter.builder;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.one.workflow.api.serde.JacksonSerde;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.util.FixedPollDelayGenerator;
import org.one.workflow.api.util.PollDelayGenerator;
import org.one.workflow.api.util.WorkflowException;
import org.one.workflow.redis.adapter.JedisQueueAdapter;
import redis.clients.jedis.JedisPool;

/**
 * Builder class for {@link org.one.workflow.api.adapter.QueueAdapter} with Redis as queue service.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JedisQueueAdapterBuilder {

  private JedisPool jedis;
  private String namespace;
  private PollDelayGenerator delayGenerator = new FixedPollDelayGenerator(Duration.ofSeconds(1L));
  private Serde serde = JacksonSerde.getInstance();

  public static JedisQueueAdapterBuilder builder() {
    return new JedisQueueAdapterBuilder();
  }

  public JedisQueueAdapterBuilder withSerde(@NonNull final Serde serde) {
    this.serde = serde;
    return this;
  }

  public JedisQueueAdapterBuilder withJedisPool(final JedisPool jedis) {
    this.jedis = jedis;
    return this;
  }

  public JedisQueueAdapterBuilder withPollDelayGenerator(
      @NonNull final PollDelayGenerator pollDelayGenerator) {
    this.delayGenerator = pollDelayGenerator;
    return this;
  }

  public JedisQueueAdapterBuilder withNamespace(final String namespace) {
    this.namespace = namespace;
    return this;
  }

  /**
   * Build instance of {@link org.one.workflow.api.adapter.QueueAdapter}.
   *
   * @return - instance of {@link JedisQueueAdapter}.
   */
  public JedisQueueAdapter build() {
    if (jedis == null) {
      throw new WorkflowException("Jedis pool cant be null");
    }
    if ((namespace == null) || namespace.isEmpty()) {
      throw new WorkflowException("Namespace cant be blank");
    }
    return new JedisQueueAdapter(jedis, serde, delayGenerator, namespace);
  }

}
