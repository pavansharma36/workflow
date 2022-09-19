package io.github.pavansharma36.workflow.jedis;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Base class for all class which can connect to redis.
 */
@RequiredArgsConstructor
public class BaseJedisAccessor {

  protected static final Charset UTF_8 = StandardCharsets.UTF_8;

  private final JedisPool jedisPool;

  protected void doInRedis(final Consumer<Jedis> consumer) {
    try (Jedis jedis = jedisPool.getResource()) {
      consumer.accept(jedis);
    }
  }

  protected <T> T getFromRedis(final Function<Jedis, T> function) {
    try (Jedis jedis = jedisPool.getResource()) {
      return function.apply(jedis);
    }
  }

  protected boolean isNil(final String value) {
    return (value == null) || "nil".equals(value);
  }

}
