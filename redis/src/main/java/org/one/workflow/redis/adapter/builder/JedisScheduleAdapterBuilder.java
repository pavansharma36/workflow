package org.one.workflow.redis.adapter.builder;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.one.workflow.api.adapter.builder.BaseAdapterBuilder;
import org.one.workflow.api.adapter.builder.BaseScheduleAdapterBuilder;
import org.one.workflow.api.util.FixedPollDelayGenerator;
import org.one.workflow.api.util.PollDelayGenerator;
import org.one.workflow.api.util.WorkflowException;
import org.one.workflow.redis.adapter.JedisScheduleAdapter;
import redis.clients.jedis.JedisPool;

/**
 * Builder class for {@link org.one.workflow.api.adapter.ScheduleAdapter} with redis as datastore.
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
   * Build instance of {@link org.one.workflow.api.adapter.ScheduleAdapter}.
   *
   * @return - instance of {@link org.one.workflow.redis.adapter.JedisQueueAdapter}.
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
