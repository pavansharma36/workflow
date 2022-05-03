package org.one.workflow.redis.adapter;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.PersistenceAdapter;
import org.one.workflow.api.bean.id.Id;
import org.one.workflow.api.bean.id.ManagerId;
import org.one.workflow.api.bean.id.RunId;
import org.one.workflow.api.bean.id.TaskId;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.model.ManagerInfo;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;
import org.one.workflow.api.serde.Deserializer;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.serde.Serializer;
import org.one.workflow.redis.BaseJedisAccessor;
import org.one.workflow.redis.WorkflowRedisKeyNamesCreator;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

/**
 * PersistenceAdapter implementation using {@link redis.clients.jedis.Jedis}.
 */
public class JedisPersistenceAdapter extends BaseJedisAccessor implements PersistenceAdapter {

  private final Serializer serializer;
  private final Deserializer deserializer;
  private final WorkflowRedisKeyNamesCreator keyNamesCreator;

  /**
   * Required contructor.
   *
   * @param jedisPool - jedisPool
   * @param serde - serde
   * @param namespace - namespace.
   */
  public JedisPersistenceAdapter(final JedisPool jedisPool, final Serde serde,
                                 final String namespace) {
    super(jedisPool);
    this.serializer = serde.serializer();
    this.deserializer = serde.deserializer();
    this.keyNamesCreator = new WorkflowRedisKeyNamesCreator(namespace);
  }

  @Override
  public void start(final WorkflowManager workflowManager) {
    doInRedis(BinaryJedis::ping);
  }

  @Override
  public void stop() {
    // nothing to do.
  }

  @Override
  public boolean createOrUpdateManagerInfo(ManagerInfo managerInfo) {
    return getFromRedis(jedis -> jedis.hset(
        keyNamesCreator.getManagerInfoKey().getBytes(),
        managerInfo.getManagerId().getId().getBytes(),
        serializer.serialize(managerInfo))) > 0;
  }

  @Override
  public List<ManagerInfo> getAllManagerInfos() {
    return getFromRedis(jedis ->
      jedis.hgetAll(keyNamesCreator.getManagerInfoKey().getBytes()).values()
          .stream().map(m -> deserializer.deserialize(m, ManagerInfo.class))
          .collect(Collectors.toList())
    );
  }

  @Override
  public boolean removeManagerInfo(ManagerId id) {
    return getFromRedis(jedis -> jedis.hdel(
        keyNamesCreator.getManagerInfoKey().getBytes(),
        id.getId().getBytes())) > 0;
  }

  @Override
  public boolean updateQueuedTime(final RunId runId, final TaskId taskId) {
    final Optional<TaskInfo> oTask = getTaskInfo(runId, taskId);
    if (oTask.isPresent()) {
      final TaskInfo t = oTask.get();
      t.setQueuedTimeEpoch(System.currentTimeMillis());
      createTaskInfos(runId, Collections.singletonList(t));
      return true;
    }
    return false;
  }

  @Override
  public boolean updateStartTime(final RunId runId) {
    final Optional<RunInfo> oRun = getRunInfo(runId);
    if (oRun.isPresent()) {
      final RunInfo runInfo = oRun.get();
      runInfo.setStartTimeEpoch(System.currentTimeMillis());
      doInRedis(
          jedis -> jedis.hset(keyNamesCreator.getRunInfoKey().getBytes(), runId.getId().getBytes(),
              serializer.serialize(runInfo)));
      return true;
    }
    return false;
  }

  @Override
  public boolean updateStartTime(final RunId runId, final TaskId taskId) {
    final Optional<TaskInfo> oTask = getTaskInfo(runId, taskId);
    if (oTask.isPresent()) {
      final TaskInfo t = oTask.get();
      t.setStartTimeEpoch(System.currentTimeMillis());
      createTaskInfos(runId, Collections.singletonList(t));
      return true;
    }
    return false;
  }

  @Override
  public boolean completeTask(final ExecutableTask executableTask,
                          final ExecutionResult executionResult) {
    final Optional<TaskInfo> oTask =
        getTaskInfo(executableTask.getRunId(), executableTask.getTaskId());
    if (oTask.isPresent()) {
      final TaskInfo t = oTask.get();
      t.setCompletionTimeEpoch(System.currentTimeMillis());
      t.setResult(executionResult);
      createTaskInfos(executableTask.getRunId(), Collections.singletonList(t));
      return true;
    }
    return false;
  }

  @Override
  public Optional<TaskInfo> getTaskInfo(final RunId runId, final TaskId taskId) {
    return getFromRedis(jedis -> {
      final String ti = jedis.hget(keyNamesCreator.getTaskInfoKey(runId), taskId.getId());
      if (isNil(ti)) {
        return Optional.empty();
      } else {
        return Optional.of(deserializer.deserialize(ti.getBytes(), TaskInfo.class));
      }
    });
  }

  @Override
  public Optional<RunInfo> getRunInfo(final RunId runId) {
    return getFromRedis(jedis -> {
      final String ti = jedis.hget(keyNamesCreator.getRunInfoKey(), runId.getId());
      if (isNil(ti)) {
        return Optional.empty();
      } else {
        return Optional.of(deserializer.deserialize(ti.getBytes(), RunInfo.class));
      }
    });
  }

  @Override
  public void createRunInfo(final RunInfo runInfo) {
    doInRedis(jedis -> jedis.hset(keyNamesCreator.getRunInfoKey().getBytes(),
        runInfo.getRunId().getId().getBytes(),
        serializer.serialize(runInfo)));
  }

  @Override
  public void createTaskInfos(final RunId runId, final List<TaskInfo> taskInfos) {
    doInRedis(jedis -> jedis.hset(keyNamesCreator.getTaskInfoKey(runId).getBytes(),
        taskInfos.stream().collect(
            Collectors.toMap(k -> k.getTaskId().getId().getBytes(), serializer::serialize))));
  }

  @Override
  public boolean cleanup(final RunId runId) {
    doInRedis(jedis -> {
      final Transaction transaction = jedis.multi();
      transaction.del(keyNamesCreator.getTaskInfoKey(runId));
      transaction.hdel(keyNamesCreator.getRunInfoKey(), runId.getId());
      transaction.exec();
    });
    return true;
  }

  @Override
  public boolean updateRunInfoEpoch(RunId runId) {
    final Optional<RunInfo> oRun = getRunInfo(runId);
    if (oRun.isPresent()) {
      final RunInfo runInfo = oRun.get();
      runInfo.setLastUpdateEpoch(System.currentTimeMillis());
      doInRedis(
          jedis -> jedis.hset(keyNamesCreator.getRunInfoKey().getBytes(), runId.getId().getBytes(),
              serializer.serialize(runInfo)));
      return true;
    }
    return false;
  }

  @Override
  public List<RunInfo> getStuckRunInfos(Duration maxDuration) {
    long currentTimeMillis = System.currentTimeMillis();
    List<String> runs = getFromRedis(jedis -> jedis.hvals(keyNamesCreator.getRunInfoKey()));
    return runs.stream().map(r -> deserializer.deserialize(r.getBytes(), RunInfo.class))
        .filter(ri -> currentTimeMillis - ri.getLastUpdateEpoch() > maxDuration.toMillis())
        .collect(Collectors.toList());
  }
}
