package io.github.pavansharma36.workflow.redis.adapter;

import io.github.pavansharma36.workflow.redis.BaseJedisAccessor;
import io.github.pavansharma36.workflow.redis.WorkflowRedisKeyNamesCreator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import io.github.pavansharma36.workflow.api.WorkflowManager;
import io.github.pavansharma36.workflow.api.adapter.QueueAdapter;
import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.bean.id.ManagerId;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.task.TaskType;
import io.github.pavansharma36.workflow.api.executor.ExecutableTask;
import io.github.pavansharma36.workflow.api.model.ManagerInfo;
import io.github.pavansharma36.workflow.api.model.TaskInfo;
import io.github.pavansharma36.workflow.api.serde.Deserializer;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.api.serde.Serializer;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import redis.clients.jedis.JedisPool;

/**
 * Queue adapter implementation using {@link redis.clients.jedis.Jedis}.
 */
@Slf4j
public class JedisQueueAdapter extends BaseJedisAccessor implements QueueAdapter {

  private static final int MAX_MAINTENANCE_ROTATION = 100;

  private final Serializer serializer;
  private final Deserializer deserializer;
  private final PollDelayGenerator pollDelayGenerator;
  private final WorkflowRedisKeyNamesCreator keyNamesCreator;

  /**
   * constructor to create instance of {@link JedisQueueAdapter}.
   *
   * @param jedisPool - jedis pool to connect to redis.
   * @param serde - to serialize/deserialize
   * @param pollDelayGenerator - to poll for messages from queue.
   * @param namespace - namespace.
   */
  public JedisQueueAdapter(final JedisPool jedisPool, final Serde serde,
                           final PollDelayGenerator pollDelayGenerator,
                           final String namespace) {
    super(jedisPool);
    this.serializer = serde.serializer();
    this.deserializer = serde.deserializer();
    this.pollDelayGenerator = pollDelayGenerator;
    this.keyNamesCreator = new WorkflowRedisKeyNamesCreator(namespace);
  }

  @Override
  public void start(final WorkflowManager workflowManager) {
    // TODO
  }

  @Override
  public void stop() {
    // TODO
  }

  @Override
  public void maintenance(WorkflowAdapter workflowAdapter) {
    List<ExecutableTask> tasks =
        getFromRedis(jedis -> jedis.lrange(keyNamesCreator.getQueuedTaskCheckKey().getBytes(UTF_8),
            -MAX_MAINTENANCE_ROTATION, -1)).stream()
            .map(t -> deserializer.deserialize(t, ExecutableTask.class)).collect(
            Collectors.toList());
    Set<ManagerId> runningWorkflowManagers = workflowAdapter.persistenceAdapter()
        .getAllManagerInfos().stream().map(ManagerInfo::getManagerId).collect(Collectors.toSet());
    tasks.forEach(t -> {
      Optional<TaskInfo> oti = workflowAdapter.persistenceAdapter()
          .getTaskInfo(t.getRunId(), t.getTaskId());
      if (oti.isPresent()) {
        TaskInfo ti = oti.get();
        if (ti.getProcessedBy() != null && !runningWorkflowManagers.contains(ti.getProcessedBy())) {
          log.warn("Task processer not running queuing again");
          commitTaskProcessed(t);
          pushTask(t);
        }
      } else {
        log.warn("Task instance not present for {}, marking as completed", t);
        commitTaskProcessed(t);
      }
    });

    int i = 0;
    while (i++ < 100 && !isNil(getFromRedis(jedis ->
        jedis.rpoplpush(keyNamesCreator.getUpdatedRunQueueCheck(),
            keyNamesCreator.getUpdatedRunQueue())))) {
      log.info("Pushed updated run");
    }

  }

  @Override
  public void pushTask(final ExecutableTask task) {
    doInRedis(jedis -> jedis.lpush(keyNamesCreator
            .getQueuedTaskKey(task.getTaskType()).getBytes(UTF_8),
        serializer.serialize(task)));
  }

  @Override
  public Optional<ExecutableTask> pollTask(final TaskType taskType) {
    return getFromRedis(jedis -> {
      final String oTask = jedis.rpoplpush(keyNamesCreator.getQueuedTaskKey(taskType),
          keyNamesCreator.getQueuedTaskCheckKey());
      if ((oTask == null) || "nil".equals(oTask)) {
        return Optional.empty();
      } else {
        return Optional.of(deserializer.deserialize(oTask.getBytes(UTF_8), ExecutableTask.class));
      }
    });
  }

  @Override
  public boolean commitTaskProcessed(ExecutableTask task) {
    return getFromRedis(jedis -> jedis.lrem(keyNamesCreator.getQueuedTaskCheckKey().getBytes(UTF_8),
        1, serializer.serialize(task))) > 0;
  }

  @Override
  public void pushUpdatedRun(final RunId runId) {
    doInRedis(jedis -> jedis.lpush(keyNamesCreator.getUpdatedRunQueue().getBytes(UTF_8),
        serializer.serialize(runId)));
  }

  @Override
  public Optional<RunId> pollUpdatedRun() {
    return getFromRedis(jedis -> {
      final String oTask = jedis.rpoplpush(keyNamesCreator.getUpdatedRunQueue(),
          keyNamesCreator.getUpdatedRunQueueCheck());
      if (isNil(oTask)) {
        return Optional.empty();
      } else {
        return Optional.of(deserializer.deserialize(oTask.getBytes(UTF_8), RunId.class));
      }
    });
  }

  @Override
  public PollDelayGenerator pollDelayGenerator() {
    return pollDelayGenerator;
  }

  @Override
  public boolean commitUpdatedRunProcess(RunId runId) {
    final String oTask = getFromRedis(jedis ->
        jedis.rpop(keyNamesCreator.getUpdatedRunQueueCheck()));
    if (isNil(oTask)) {
      return false;
    } else {
      RunId r = deserializer.deserialize(oTask.getBytes(UTF_8), RunId.class);
      if (runId.equals(r)) {
        return true;
      } else {
        doInRedis(jedis -> jedis.rpush(keyNamesCreator.getUpdatedRunQueueCheck().getBytes(UTF_8),
            serializer.serialize(r)));
        return false;
      }
    }
  }
}
