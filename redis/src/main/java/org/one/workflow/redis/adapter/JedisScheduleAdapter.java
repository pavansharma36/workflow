package org.one.workflow.redis.adapter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.ScheduleAdapter;
import org.one.workflow.api.util.PollDelayGenerator;
import org.one.workflow.redis.BaseJedisAccessor;
import org.one.workflow.redis.WorkflowRedisKeyNamesCreator;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

public class JedisScheduleAdapter extends BaseJedisAccessor implements ScheduleAdapter {

  private final WorkflowRedisKeyNamesCreator keyNamesCreator;
  private final AtomicBoolean leader = new AtomicBoolean(false);
  private final PollDelayGenerator pollDelayGenerator;
  private final PollDelayGenerator maintenanceDelayGenerator;
  private final PollDelayGenerator heartbeatDelayGenerator;
  private final Duration maxRunDuration;

  public JedisScheduleAdapter(final JedisPool jedisPool, final String namespace,
                              final PollDelayGenerator pollDelayGenerator,
                              PollDelayGenerator maintenanceDelayGenerator,
                              PollDelayGenerator heartbeatDelayGenerator,
                              Duration maxRunDuration) {
    super(jedisPool);
    this.keyNamesCreator = new WorkflowRedisKeyNamesCreator(namespace);
    this.pollDelayGenerator = pollDelayGenerator;
    this.maintenanceDelayGenerator = maintenanceDelayGenerator;
    this.heartbeatDelayGenerator = heartbeatDelayGenerator;
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
  public PollDelayGenerator heartbeatDelayGenerator() {
    return heartbeatDelayGenerator;
  }

  @Override
  public Duration maxRunDuration() {
    return maxRunDuration;
  }

  @Override
  public void stop() {

  }

  @Override
  public boolean isScheduler() {
    return leader.get();
  }

}
