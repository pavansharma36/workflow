package io.github.pavansharma36.workflow.jedis.adapter.builder;

import io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.BaseAdapterBuilder;
import io.github.pavansharma36.workflow.api.adapter.builder.BasePersistenceAdapterBuilder;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.api.util.FixedPollDelayGenerator;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import io.github.pavansharma36.workflow.api.util.WorkflowException;
import io.github.pavansharma36.workflow.jackson.serde.JacksonSerde;
import io.github.pavansharma36.workflow.jedis.adapter.JedisPersistenceAdapter;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import redis.clients.jedis.JedisPool;

/**
 * Builder for {@link io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter}
 * with Redis as datastore.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JedisPersistenceAdapterBuilder
    extends BasePersistenceAdapterBuilder<JedisPersistenceAdapterBuilder> {

  private JedisPool jedis;
  private Serde serde;

  public static JedisPersistenceAdapterBuilder builder() {
    return new JedisPersistenceAdapterBuilder();
  }

  public JedisPersistenceAdapterBuilder withJedisPool(final JedisPool pool) {
    this.jedis = pool;
    return this;
  }

  /**
   * Build {@link io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter}
   * with given details.
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
    if (serde == null) {
      serde = JacksonSerde.getInstance();
    }
    return new JedisPersistenceAdapter(jedis, serde, namespace, pollDelayGenerator);
  }

  public JedisPersistenceAdapterBuilder withSerde(Serde serde) {
    this.serde = serde;
    return this;
  }

}
