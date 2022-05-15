package org.one.workflow.redis.adapter.builder;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.one.workflow.api.util.FixedPollDelayGenerator;
import org.one.workflow.api.util.PollDelayGenerator;
import org.one.workflow.api.util.WorkflowException;
import org.one.workflow.redis.adapter.JedisScheduleAdapter;
import redis.clients.jedis.JedisPool;

/**
 * Builder class for {@link org.one.workflow.api.adapter.ScheduleAdapter} with redis as datastore.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JedisScheduleAdapterBuilder {

  private JedisPool jedis;
  private String namespace;
  private PollDelayGenerator pollDelayGenerator = new FixedPollDelayGenerator(
      Duration.ofSeconds(1L));
  private PollDelayGenerator maintenanceDelayGenerator = new FixedPollDelayGenerator(
      Duration.ofHours(1L));
  private Duration maxRunDuration = Duration.ofDays(7L);

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
    this.pollDelayGenerator = pollDelayGenerator;
    return this;
  }

  public JedisScheduleAdapterBuilder withMaintenanceDelayGenerator(
      @NonNull final PollDelayGenerator maintenanceDelayGenerator) {
    this.maintenanceDelayGenerator = maintenanceDelayGenerator;
    return this;
  }

  public JedisScheduleAdapterBuilder maxRunDuration(@NonNull Duration maxRunDuration) {
    this.maxRunDuration = maxRunDuration;
    return this;
  }

  /**
   * Build instance of {@link org.one.workflow.api.adapter.ScheduleAdapter}.
   *
   * @return - instance of {@link org.one.workflow.redis.adapter.JedisQueueAdapter}.
   */
  public JedisScheduleAdapter build() {
    if (jedis == null) {
      throw new WorkflowException("Jedis pool can't be null");
    }
    if ((namespace == null) || namespace.isEmpty()) {
      throw new WorkflowException("Namespace cant be blank");
    }
    return new JedisScheduleAdapter(jedis, namespace, pollDelayGenerator,
        maintenanceDelayGenerator, maxRunDuration);
  }

}
