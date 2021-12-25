package org.one.workflow.redis.adapter.builder;

import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.adapter.WorkflowAdapterImpl;
import org.one.workflow.api.serde.JacksonSerde;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.util.PollDelayGenerator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import redis.clients.jedis.JedisPool;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JedisWorkflowAdapterBuilder {

	private JedisScheduleAdapterBuilder jedisScheduleAdapterBuilder;
	private JedisPersistenceAdapterBuilder jedisPersistenceAdapterBuilder;
	private JedisQueueAdapterBuilder jedisQueueAdapterBuilder;

	public static JedisWorkflowAdapterBuilder builder(final JedisPool jedisPool, final String namespace) {
		return builder(jedisPool, namespace, JacksonSerde.getInstance());
	}

	public static JedisWorkflowAdapterBuilder builder(final JedisPool jedisPool, final String namespace,
			final Serde serde) {
		final JedisWorkflowAdapterBuilder builder = new JedisWorkflowAdapterBuilder();
		builder.jedisScheduleAdapterBuilder = JedisScheduleAdapterBuilder.builder().withJedisPool(jedisPool)
				.withNamespace(namespace);
		builder.jedisQueueAdapterBuilder = JedisQueueAdapterBuilder.builder().withJedisPool(jedisPool)
				.withNamespace(namespace).withSerde(serde);
		builder.jedisPersistenceAdapterBuilder = JedisPersistenceAdapterBuilder.builder().withJedisPool(jedisPool)
				.withNamespace(namespace).withSerde(serde);
		return builder;
	}

	public JedisWorkflowAdapterBuilder withQueuePollDelayGenerator(final PollDelayGenerator pollDelayGenerator) {
		this.jedisQueueAdapterBuilder.withPollDelayGenerator(pollDelayGenerator);
		return this;
	}

	public JedisWorkflowAdapterBuilder withSchedulePollDelayGenerator(final PollDelayGenerator pollDelayGenerator) {
		this.jedisScheduleAdapterBuilder.withPollDelayGenerator(pollDelayGenerator);
		return this;
	}

	public WorkflowAdapter build() {
		return new WorkflowAdapterImpl(jedisScheduleAdapterBuilder.build(), jedisQueueAdapterBuilder.build(),
				jedisPersistenceAdapterBuilder.build());
	}

}
