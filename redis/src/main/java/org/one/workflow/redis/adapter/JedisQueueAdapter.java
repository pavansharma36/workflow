package org.one.workflow.redis.adapter;

import java.util.Optional;

import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.QueueAdapter;
import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.serde.Deserializer;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.serde.Serializer;
import org.one.workflow.api.util.PollDelayGenerator;
import org.one.workflow.redis.BaseJedisAccessor;
import org.one.workflow.redis.WorkflowRedisKeyNamesCreator;

import redis.clients.jedis.JedisPool;

public class JedisQueueAdapter extends BaseJedisAccessor implements QueueAdapter {

	private final Serializer serializer;
	private final Deserializer deserializer;
	private final PollDelayGenerator pollDelayGenerator;
	private final WorkflowRedisKeyNamesCreator keyNamesCreator;

	public JedisQueueAdapter(final JedisPool jedisPool, final Serde serde, final PollDelayGenerator pollDelayGenerator,
			final String namespace) {
		super(jedisPool);
		this.serializer = serde.serializer();
		this.deserializer = serde.deserializer();
		this.pollDelayGenerator = pollDelayGenerator;
		this.keyNamesCreator = new WorkflowRedisKeyNamesCreator(namespace);
	}

	@Override
	public void start(final WorkflowManager workflowManager) {

	}

	@Override
	public void stop() {

	}

	@Override
	public void pushTask(final ExecutableTask task) {
		doInRedis(jedis -> {
			jedis.lpush(keyNamesCreator.getQueuedTaskKey(task.getTaskType()).getBytes(), serializer.serialize(task));
		});
	}

	@Override
	public Optional<ExecutableTask> pollTask(final TaskType taskType) {
		return getFromRedis(jedis -> {
			final String oTask = jedis.rpoplpush(keyNamesCreator.getQueuedTaskKey(taskType),
					keyNamesCreator.getQueuedTaskCheckKey());
			if ((oTask == null) || "nil".equals(oTask)) {
				return Optional.empty();
			} else {
				return Optional.of(deserializer.deserialize(oTask.getBytes(), ExecutableTask.class));
			}
		});
	}

	@Override
	public void pushUpdatedRun(final RunId runId) {
		doInRedis(jedis -> jedis.lpush(keyNamesCreator.getUpdatedRunQueue().getBytes(), serializer.serialize(runId)));
	}

	@Override
	public Optional<RunId> pollUpdatedRun() {
		return getFromRedis(jedis -> {
			final String oTask = jedis.rpoplpush(keyNamesCreator.getUpdatedRunQueue(),
					keyNamesCreator.getUpdatedRunQueueCheck());
			if (isNil(oTask)) {
				return Optional.empty();
			} else {
				return Optional.of(deserializer.deserialize(oTask.getBytes(), RunId.class));
			}
		});
	}

	@Override
	public PollDelayGenerator pollDelayGenerator() {
		return pollDelayGenerator;
	}

}
