package org.one.workflow.redis.adapter;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.PersistenceAdapter;
import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;
import org.one.workflow.api.serde.Deserializer;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.serde.Serializer;
import org.one.workflow.redis.BaseJedisAccessor;
import org.one.workflow.redis.WorkflowRedisKeyNamesCreator;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

public class JedisPersistenceAdapter extends BaseJedisAccessor implements PersistenceAdapter {

	private final Serializer serializer;
	private final Deserializer deserializer;
	private final WorkflowRedisKeyNamesCreator keyNamesCreator;

	public JedisPersistenceAdapter(final JedisPool jedisPool, final Serde serde, final String namespace) {
		super(jedisPool);
		this.serializer = serde.serializer();
		this.deserializer = serde.deserializer();
		this.keyNamesCreator = new WorkflowRedisKeyNamesCreator(namespace);
	}

	@Override
	public void start(final WorkflowManager workflowManager) {

	}

	@Override
	public void stop() {

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
	public int completeTask(final ExecutableTask executableTask, final ExecutionResult executionResult) {
		final Optional<TaskInfo> oTask = getTaskInfo(executableTask.getRunId(), executableTask.getTaskId());
		if (oTask.isPresent()) {
			final TaskInfo t = oTask.get();
			t.setCompletionTimeEpoch(System.currentTimeMillis());
			t.setMessage(executionResult.getMessage());
			t.setStatus(executionResult.getStatus());
			t.setResultMeta(executionResult.getResultMeta());
			t.setDecisionValue(executionResult.getDecision());
			createTaskInfos(executableTask.getRunId(), Collections.singletonList(t));
			return 1;
		}
		return 0;
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
		doInRedis(jedis -> jedis.hset(keyNamesCreator.getRunInfoKey().getBytes(), runInfo.getRunId().getBytes(),
				serializer.serialize(runInfo)));
	}

	@Override
	public void createTaskInfos(final RunId runId, final List<TaskInfo> taskInfos) {
		doInRedis(jedis -> jedis.hset(keyNamesCreator.getTaskInfoKey(runId).getBytes(),
				taskInfos.stream().collect(Collectors.toMap(k -> k.getTaskId().getId().getBytes(), serializer::serialize))));
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
	public boolean updateStartTime(final RunId runId) {
		final Optional<RunInfo> oRun = getRunInfo(runId);
		if (oRun.isPresent()) {
			final RunInfo runInfo = oRun.get();
			runInfo.setStartTimeEpoch(System.currentTimeMillis());
			doInRedis(jedis -> jedis.hset(keyNamesCreator.getRunInfoKey().getBytes(), runId.getId().getBytes(),
					serializer.serialize(runInfo)));
			return true;
		}
		return false;
	}

	@Override
	public List<RunInfo> getStuckRunInfos(Duration maxDuration) {
		return Collections.emptyList();
	}
}
