package io.github.pavansharma36.workflow.redis.adapter;

import io.github.pavansharma36.workflow.redis.BaseJedisAccessor;
import io.github.pavansharma36.workflow.redis.WorkflowRedisKeyNamesCreator;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.ScheduleAdapter;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.util.PollDelayGenerator;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

/**
 * ScheduleAdapter implementation using {@link redis.clients.jedis.Jedis}.
 */
public class JedisScheduleAdapter extends BaseJedisAccessor implements ScheduleAdapter {

  private final WorkflowRedisKeyNamesCreator keyNamesCreator;
  private final AtomicBoolean leader = new AtomicBoolean(false);
  private final PollDelayGenerator pollDelayGenerator;
  private final PollDelayGenerator maintenanceDelayGenerator;
  private final Duration maxRunDuration;

  /**
   * Constructor to create instance of {@link JedisScheduleAdapter}.
   *
   * @param jedisPool - pool of jedis to connect to redis.
   * @param namespace - namespace.
   * @param pollDelayGenerator - for polling for leadership.
   * @param maintenanceDelayGenerator - to run maintenance.
   * @param maxRunDuration - max duration of workflow run.
   */
  public JedisScheduleAdapter(final JedisPool jedisPool, final String namespace,
                              final PollDelayGenerator pollDelayGenerator,
                              PollDelayGenerator maintenanceDelayGenerator,
                              Duration maxRunDuration) {
    super(jedisPool);
    this.keyNamesCreator = new WorkflowRedisKeyNamesCreator(namespace);
    this.pollDelayGenerator = pollDelayGenerator;
    this.maintenanceDelayGenerator = maintenanceDelayGenerator;
    this.maxRunDuration = maxRunDuration;
  }

  @Override
  public void start(final WorkflowManager workflowManager) {
    final Runnable runnable = () -> {
      final String runId = workflowManager.info().getManagerId().getId();
      if (leader.get()) {
        final String value =
            getFromRedis(jedis -> jedis.get(keyNamesCreator.getLeaderElectionKey()));
        if (runId.equals(value)) {
          doInRedis(jedis -> jedis.expire(keyNamesCreator.getLeaderElectionKey(), 50L));
        } else {
          leader.set(false);
        }
      } else {
        tryLeadership(runId);
      }
    };
    workflowManager.scheduledExecutorService()
        .scheduleAtFixedRate(runnable, 0, 30, TimeUnit.SECONDS);
  }

  @Override
  public void stop() {
    if (leader.get()) {
      doInRedis(jedis -> jedis.del(keyNamesCreator.getLeaderElectionKey()));
    }
  }

  @Override
  public void maintenance(WorkflowAdapter adapter) {
    // TODO
  }

  private boolean tryLeadership(final String runId) {
    final SetParams setParams = SetParams.setParams().nx().ex(50L);
    final String update = getFromRedis(
        jedis -> jedis.set(keyNamesCreator.getLeaderElectionKey(), runId, setParams));
    leader.set(!isNil(update));
    return leader.get();
  }

  @Override
  public PollDelayGenerator pollDelayGenerator() {
    return pollDelayGenerator;
  }

  @Override
  public PollDelayGenerator maintenanceDelayGenerator() {
    return maintenanceDelayGenerator;
  }

  @Override
  public Duration maxRunDuration() {
    return maxRunDuration;
  }

  @Override
  public boolean isScheduler() {
    return leader.get();
  }

}
