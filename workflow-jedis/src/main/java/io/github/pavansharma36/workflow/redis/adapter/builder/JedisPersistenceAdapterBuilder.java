package io.github.pavansharma36.workflow.redis.adapter.builder;

import io.github.pavansharma36.workflow.redis.adapter.JedisPersistenceAdapter;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.one.workflow.api.adapter.PersistenceAdapter;
import org.one.workflow.api.adapter.builder.BaseAdapterBuilder;
import org.one.workflow.api.serde.JacksonSerde;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.util.FixedPollDelayGenerator;
import org.one.workflow.api.util.PollDelayGenerator;
import org.one.workflow.api.util.WorkflowException;
import redis.clients.jedis.JedisPool;

/**
 * Builder for {@link org.one.workflow.api.adapter.PersistenceAdapter} with Redis as datastore.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JedisPersistenceAdapterBuilder
    extends BaseAdapterBuilder<JedisPersistenceAdapterBuilder, PersistenceAdapter> {

  private JedisPool jedis;

  public static JedisPersistenceAdapterBuilder builder() {
    return new JedisPersistenceAdapterBuilder();
  }

  public JedisPersistenceAdapterBuilder withJedisPool(final JedisPool pool) {
    this.jedis = pool;
    return this;
  }

  public JedisPersistenceAdapterBuilder heartbeatDelayGenerator(
      @NonNull final PollDelayGenerator heartbeatDelayGenerator) {
    this.pollDelayGenerator = heartbeatDelayGenerator;
    return this;
  }

  /**
   * Build {@link org.one.workflow.api.adapter.PersistenceAdapter} with given details.
   *
   * @return instance of {@link JedisPersistenceAdapter}.
   */
  public JedisPersistenceAdapter build() {
    if (pollDelayGenerator == null) {
      pollDelayGenerator = new FixedPollDelayGenerator(Duration.ofSeconds(30L));
    }
    validate();
    if (jedis == null) {
      throw new WorkflowException("Jedis pool can't be null");
    }
    return new JedisPersistenceAdapter(jedis, serde, namespace, pollDelayGenerator);
  }

}
