package org.one.workflow.redis.adapter.builder;

import org.one.workflow.api.serde.JacksonSerde;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.redis.adapter.JedisPersistenceAdapter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import redis.clients.jedis.JedisPool;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JedisPersistenceAdapterBuilder {

	private JedisPool jedis;
	private String namespace;
	private Serde serde = JacksonSerde.getInstance();

	public static JedisPersistenceAdapterBuilder builder() {
		return new JedisPersistenceAdapterBuilder();
	}

	public JedisPersistenceAdapterBuilder withJedisPool(final JedisPool pool) {
		this.jedis = pool;
		return this;
	}

	public JedisPersistenceAdapterBuilder withNamespace(final String namespace) {
		this.namespace = namespace;
		return this;
	}

	public JedisPersistenceAdapterBuilder withSerde(@NonNull final Serde serde) {
		this.serde = serde;
		return this;
	}

	public JedisPersistenceAdapter build() {
		if (jedis == null) {
			throw new RuntimeException("Jedis pool can't be null");
		}
		if ((namespace == null) || namespace.isBlank()) {
			throw new RuntimeException("Namespace cant be blank");
		}
		return new JedisPersistenceAdapter(jedis, serde, namespace);
	}

}
