package org.one.workflow.redis.adapter;

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

  public JedisScheduleAdapter(final JedisPool jedisPool, final String namespace,
                              final PollDelayGenerator pollDelayGenerator) {
    super(jedisPool);
    this.keyNamesCreator = new WorkflowRedisKeyNamesCreator(namespace);
    this.pollDelayGenerator = pollDelayGenerator;
  }

  @Override
  public void start(final WorkflowManager workflowManager) {
    final Runnable runnable = () -> {
      final String runId = workflowManager.workflowManagerId();
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
  public void stop() {

  }

  @Override
  public boolean isScheduler() {
    return leader.get();
  }

}
