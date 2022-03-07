package org.one.workflow.redis.adapter.builder;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.one.workflow.api.util.FixedPollDelayGenerator;
import org.one.workflow.api.util.PollDelayGenerator;
import org.one.workflow.redis.adapter.JedisScheduleAdapter;
import redis.clients.jedis.JedisPool;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JedisScheduleAdapterBuilder {

  private JedisPool jedis;
  private String namespace;
  private PollDelayGenerator delayGenerator = new FixedPollDelayGenerator(Duration.ofSeconds(1L));

  public static JedisScheduleAdapterBuilder builder() {
    return new JedisScheduleAdapterBuilder();
  }

  public JedisScheduleAdapterBuilder withJedisPool(final JedisPool jedis) {
    this.jedis = jedis;
    return this;
  }

  public JedisScheduleAdapterBuilder withNamespace(final String namespace) {
    this.namespace = namespace;
    return this;
  }

  public JedisScheduleAdapterBuilder withPollDelayGenerator(
      @NonNull final PollDelayGenerator pollDelayGenerator) {
    this.delayGenerator = pollDelayGenerator;
    return this;
  }

  public JedisScheduleAdapter build() {
    if (jedis == null) {
      throw new RuntimeException("Jedis pool can't be null");
    }
    if ((namespace == null) || namespace.isEmpty()) {
      throw new RuntimeException("Namespace cant be blank");
    }
    return new JedisScheduleAdapter(jedis, namespace, delayGenerator);
  }

}
