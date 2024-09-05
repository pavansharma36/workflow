package io.github.pavansharma36.workflow.mongodb.adapter.builder;

import com.mongodb.client.MongoClient;
import io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.BasePersistenceAdapterBuilder;
import io.github.pavansharma36.workflow.mongodb.adapter.MongoDbPersistenceAdapter;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * persistence adapter builder using mongo client.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MongoDbPersistanceAdapterBuilder
    extends BasePersistenceAdapterBuilder<MongoDbPersistanceAdapterBuilder> {

  private final String database;
  private final MongoClient mongoClient;

  /**
   * require args builder.
   *
   * @param database - database
   * @param mongoClient - mongo client
   * @return - this
   */
  public static MongoDbPersistanceAdapterBuilder builder(@NonNull String database,
                                                         @NonNull MongoClient mongoClient) {
    return new MongoDbPersistanceAdapterBuilder(database, mongoClient);
  }

  @Override
  public PersistenceAdapter build() {
    return new MongoDbPersistenceAdapter(namespace, pollDelayGenerator,
        database, mongoClient, serde);
  }
}
