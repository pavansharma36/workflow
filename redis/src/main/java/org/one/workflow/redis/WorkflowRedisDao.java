package org.one.workflow.redis;

import java.util.Optional;

import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;
import org.one.workflow.api.queue.WorkflowDao;
import org.one.workflow.api.serde.Deserializer;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.serde.Serializer;

import redis.clients.jedis.JedisPool;

public class WorkflowRedisDao extends BaseJedisAccessor implements WorkflowDao {

	private final WorkflowRedisKeyNamesCreator keyNamesCreator;
	private final Serializer serializer;
	private final Deserializer deserializer;
	
	protected WorkflowRedisDao(String namespace, JedisPool jedisPool, Serde serde) {
		super(jedisPool);
		this.keyNamesCreator = new WorkflowRedisKeyNamesCreator(namespace);
		this.serializer = serde.serializer();
		this.deserializer = serde.deserializer();
	}
	
	@Override
	public void pushTask(ExecutableTask task) {
		doInRedis(jedis -> {
			jedis.lpush(keyNamesCreator.getQueuedTaskKey(task.getTaskType()).getBytes(), serializer.serialize(task));
		});
	}

	@Override
	public Optional<ExecutableTask> pollTask(TaskType taskType) {
		return getFromRedis(jedis -> {
			String oTask = jedis.rpoplpush(keyNamesCreator.getQueuedTaskKey(taskType),
					keyNamesCreator.getQueuedTaskCheckKey());
			if (oTask == null || "nil".equals(oTask)) {
				return Optional.empty();
			} else {
				return Optional.of(deserializer.deserialize(oTask.getBytes(), ExecutableTask.class));
			}
		});
	}

	@Override
	public void pushUpdatedRun(RunId runId) {
		doInRedis(jedis -> jedis.lpush(keyNamesCreator.getUpdatedRunQueue().getBytes(), serializer.serialize(runId)));
	}

	@Override
	public Optional<RunId> pollUpdatedRun() {
		return getFromRedis(jedis -> {
			String oTask = jedis.rpoplpush(keyNamesCreator.getUpdatedRunQueue(), keyNamesCreator.getUpdatedRunQueueCheck());
			if(isNil(oTask)) {
				return Optional.empty();
			} else {
				return Optional.of(deserializer.deserialize(oTask.getBytes(), RunId.class));
			}
		});
	}

	@Override
	public int updateQueuedTime(RunId runId, TaskId taskId) {
		getTaskInfo(runId, taskId).ifPresent(t -> {
			t.setQueuedTimeEpoch(System.currentTimeMillis());
			createTaskInfo(runId, t);
		});
		return 1;
	}

	@Override
	public int updateStartTime(RunId runId, TaskId taskId) {
		getTaskInfo(runId, taskId).ifPresent(t -> {
			t.setStartTimeEpoch(System.currentTimeMillis());
			createTaskInfo(runId, t);
		});
		return 1;
	}

	@Override
	public int completeTask(RunId runId, TaskId taskId, ExecutionResult executionResult) {
		getTaskInfo(runId, taskId).ifPresent(t -> {
			t.setCompletionTimeEpoch(System.currentTimeMillis());
			t.setMessage(executionResult.getMessage());
			t.setStatus(executionResult.getStatus());
			t.setResultMeta(executionResult.getResultMeta());
			createTaskInfo(runId, t);
		});
		return 1;
	}

	@Override
	public Optional<TaskInfo> getTaskInfo(RunId runId, TaskId taskId) {
		return getFromRedis(jedis -> {
			String ti = jedis.hget(keyNamesCreator.getTaskInfoKey(runId), taskId.getId());
			if(isNil(ti)) {
				return Optional.empty();
			} else {
				return Optional.of(deserializer.deserialize(ti.getBytes(), TaskInfo.class));
			}
		});
	}

	@Override
	public Optional<RunInfo> getRunInfo(RunId runId) {
		return getFromRedis(jedis -> {
			String ti = jedis.hget(keyNamesCreator.getRunInfoKey(), runId.getId());
			if(isNil(ti)) {
				return Optional.empty();
			} else {
				return Optional.of(deserializer.deserialize(ti.getBytes(), RunInfo.class));
			}
		});
	}

	@Override
	public void createRunInfo(RunInfo runInfo) {
		doInRedis(jedis -> jedis.hset(keyNamesCreator.getRunInfoKey().getBytes(), runInfo.getRunId().getBytes(), serializer.serialize(runInfo)));
	}

	@Override
	public void createTaskInfo(RunId runId, TaskInfo taskInfo) {
		doInRedis(jedis -> jedis.hset(keyNamesCreator.getTaskInfoKey(runId).getBytes(), taskInfo.getTaskId().getBytes(), serializer.serialize(taskInfo)));
	}
	
	private boolean isNil(String value) {
		return value == null || "nil".equals(value);
	}
	
	@Override
	public void cleanup(RunId runId) {
		doInRedis(jedis -> {
			jedis.del(keyNamesCreator.getTaskInfoKey(runId));
			jedis.hdel(keyNamesCreator.getRunInfoKey(), runId.getId());
		});
	}
	

}
