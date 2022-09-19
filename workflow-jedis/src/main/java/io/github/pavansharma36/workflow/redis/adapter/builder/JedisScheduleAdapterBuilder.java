package io.github.pavansharma36.workflow.redis.adapter.builder;

import io.github.pavansharma36.workflow.redis.adapter.JedisQueueAdapter;
import io.github.pavansharma36.workflow.redis.adapter.JedisScheduleAdapter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import io.github.pavansharma36.workflow.api.adapter.builder.BaseAdapterBuilder;
import io.github.pavansharma36.workflow.api.adapter.builder.BaseScheduleAdapterBuilder;
import io.github.pavansharma36.workflow.api.util.FixedPollDelayGenerator;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import io.github.pavansharma36.workflow.api.util.WorkflowException;
import redis.clients.jedis.JedisPool;

/**
 * Builder class for {@link io.github.pavansharma36.workflow.api.adapter.ScheduleAdapter} with redis as datastore.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JedisScheduleAdapterBuilder extends
    BaseScheduleAdapterBuilder<JedisScheduleAdapterBuilder> {

  private JedisPool jedis;

  public static JedisScheduleAdapterBuilder builder() {
    return new JedisScheduleAdapterBuilder();
  }

  public JedisScheduleAdapterBuilder withJedisPool(final JedisPool jedis) {
    this.jedis = jedis;
    return this;
  }

  /**
   * Build instance of {@link io.github.pavansharma36.workflow.api.adapter.ScheduleAdapter}.
   *
   * @return - instance of {@link JedisQueueAdapter}.
   */
  public JedisScheduleAdapter build() {
    validate();
    if (jedis == null) {
      throw new WorkflowException("Jedis pool can't be null");
    }
    return new JedisScheduleAdapter(jedis, namespace, pollDelayGenerator,
        maintenanceDelayGenerator, maxRunDuration);
  }

}
