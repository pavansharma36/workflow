package org.one.workflow.redis.adapter.builder;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.one.workflow.api.serde.JacksonSerde;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.util.FixedPollDelayGenerator;
import org.one.workflow.api.util.PollDelayGenerator;
import org.one.workflow.redis.adapter.JedisPersistenceAdapter;
import redis.clients.jedis.JedisPool;

/**
 * Builder for {@link org.one.workflow.api.adapter.PersistenceAdapter} with Redis as datastore.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JedisPersistenceAdapterBuilder {

  private JedisPool jedis;
  private String namespace;
  private Serde serde = JacksonSerde.getInstance();
  private PollDelayGenerator heartbeatDelayGenerator = new FixedPollDelayGenerator(
      Duration.ofSeconds(30L));

  public static JedisPersistenceAdapterBuilder builder() {
    return new JedisPersistenceAdapterBuilder();
  }

  public JedisPersistenceAdapterBuilder withJedisPool(final JedisPool pool) {
    this.jedis = pool;
    return this;
  }

  public JedisPersistenceAdapterBuilder withNamespace(final String namespace) {
    this.namespace = namespace;
    return this;
  }

  public JedisPersistenceAdapterBuilder withSerde(@NonNull final Serde serde) {
    this.serde = serde;
    return this;
  }

  public JedisPersistenceAdapterBuilder heartbeatDelayGenerator(
      @NonNull final PollDelayGenerator heartbeatDelayGenerator) {
    this.heartbeatDelayGenerator = heartbeatDelayGenerator;
    return this;
  }

  /**
   * Build {@link org.one.workflow.api.adapter.PersistenceAdapter} with given details.
   *
   * @return instance of {@link JedisPersistenceAdapter}.
   */
  public JedisPersistenceAdapter build() {
    if (jedis == null) {
      throw new RuntimeException("Jedis pool can't be null");
    }
    if ((namespace == null) || namespace.isEmpty()) {
      throw new RuntimeException("Namespace cant be blank");
    }
    return new JedisPersistenceAdapter(jedis, serde, namespace, heartbeatDelayGenerator);
  }

}
