package org.one.workflow.redis;

import java.util.function.Consumer;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RequiredArgsConstructor
public class BaseJedisAccessor {

	private final JedisPool jedisPool;

	protected void doInRedis(final Consumer<Jedis> consumer) {
		try (Jedis jedis = jedisPool.getResource()) {
			consumer.accept(jedis);
			jedisPool.returnResource(jedis);
		}
	}

	protected <T> T getFromRedis(final Function<Jedis, T> function) {
		try (Jedis jedis = jedisPool.getResource()) {
			final T value = function.apply(jedis);
			jedisPool.returnResource(jedis);
			return value;
		}
	}

}
