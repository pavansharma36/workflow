package org.one.workflow.redis;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.one.workflow.api.schedule.ScheduleSelector;
import org.one.workflow.api.util.Utils;
import redis.clients.jedis.JedisPool;

@Slf4j
public class RedisScheduleSelector extends BaseJedisAccessor implements ScheduleSelector {

  private final String runId = Utils.random();
  private final AtomicBoolean leader = new AtomicBoolean(false);

  private final WorkflowRedisKeyNamesCreator keyNamesCreator;
  private final ScheduledExecutorService scheduledExecutorService;

  public RedisScheduleSelector(JedisPool jedisPool, String namespace,
                               ScheduledExecutorService scheduledExecutorService) {
    super(jedisPool);
    this.keyNamesCreator = new WorkflowRedisKeyNamesCreator(namespace);
    this.scheduledExecutorService = scheduledExecutorService;
  }

  @Override
  public void start() {
    Runnable runnable = () -> {
      if (leader.get()) {
        String value = getFromRedis(jedis -> jedis.get(keyNamesCreator.getLeaderElectionKey()));
        if (runId.equals(value)) {
          doInRedis(jedis -> jedis.expire(keyNamesCreator.getLeaderElectionKey(), 50L));
        } else {
          leader.set(false);
        }
      } else {
        if (tryLeadership()) {
          doInRedis(jedis -> jedis.expire(keyNamesCreator.getLeaderElectionKey(), 50L));
        }
      }
    };
    scheduledExecutorService.scheduleAtFixedRate(runnable, 0, 30, TimeUnit.SECONDS);
  }

  private boolean tryLeadership() {
    long update = getFromRedis(jedis -> jedis.setnx(keyNamesCreator.getLeaderElectionKey(), runId));
    log.info("Try leadership {}", update);
    leader.set(update != 0L);
    return leader.get();
  }

  @Override
  public boolean isScheduler() {
    return leader.get();
  }

  @Override
  public void stop() {
    if (leader.get()) {
      doInRedis(jedis -> jedis.del(keyNamesCreator.getLeaderElectionKey()));
      ;
    }
  }

}
