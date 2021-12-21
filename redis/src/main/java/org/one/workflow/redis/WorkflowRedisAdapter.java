package org.one.workflow.redis;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import org.one.workflow.api.core.WorkflowAdapter;
import org.one.workflow.api.queue.WorkflowDao;
import org.one.workflow.api.schedule.ScheduleSelector;
import org.one.workflow.api.serde.JacksonSerde;
import org.one.workflow.api.serde.Serde;

import lombok.Builder;
import lombok.NonNull;
import redis.clients.jedis.JedisPool;

@Builder
public class WorkflowRedisAdapter implements WorkflowAdapter {
	
	@Builder.Default
	private Duration pollDuration = Duration.ofSeconds(1);
	
	@Builder.Default
	private Duration schedulerPollDuration = Duration.ofSeconds(1);
	
	@NonNull
	private final JedisPool jedisPool;
	
	@NonNull
	private final String namespace;
	
	@Builder.Default
	private Serde serde = JacksonSerde.getInstance();

	@Override
	public void preStart() {
		jedisPool.getResource().ping();
	}

	@Override
	public void postStart() {
		
	}

	@Override
	public WorkflowDao workflowDao() {
		return new WorkflowRedisDao(namespace, jedisPool, serde);
	}

	@Override
	public Duration pollDuration() {
		return pollDuration;
	}
	
	@Override
	public Duration schedulerPollDuration() {
		return schedulerPollDuration;
	}

	@Override
	public ScheduleSelector scheduleSelector(ScheduledExecutorService executorService) {
		return new RedisScheduleSelector(jedisPool, namespace, executorService);
	}

	@Override
	public void preClose() {
		
	}

	@Override
	public void postClose() {
		
	}


}
