package org.one.workflow.redis;

import java.util.function.Consumer;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

@RequiredArgsConstructor
public class BaseJedisAccessor {
	
	private final JedisPool jedisPool;

	protected void doInRedis(Consumer<Jedis> consumer) {
		Jedis jedis = jedisPool.getResource();
		try {
			consumer.accept(jedis);
			jedisPool.returnResource(jedis);
		} catch (JedisConnectionException e) {
			jedisPool.returnBrokenResource(jedis);
		}
	}
	
	protected <T> T getFromRedis(Function<Jedis, T> function) {
		Jedis jedis = jedisPool.getResource();
		try {
			T value = function.apply(jedis);
			jedisPool.returnResource(jedis);
			return value;
		} catch (JedisConnectionException e) {
			jedisPool.returnBrokenResource(jedis);
			throw new RuntimeException(e);
		}
	}
	
}
