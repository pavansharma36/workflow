package io.github.pavansharma36.workflow.mongodb.adapter.builder;

import com.mongodb.client.MongoClient;
import io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.BasePersistenceAdapterBuilder;
import io.github.pavansharma36.workflow.mongodb.adapter.MongoDBPersistenceAdapter;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MongoDBPersistanceAdapterBuilder extends BasePersistenceAdapterBuilder<MongoDBPersistanceAdapterBuilder> {

  private final String database;
  private final MongoClient mongoClient;

  public static MongoDBPersistanceAdapterBuilder builder(@NonNull String database, @NonNull MongoClient mongoClient) {
    return new MongoDBPersistanceAdapterBuilder(database, mongoClient);
  }

  @Override
  public PersistenceAdapter build() {
    return new MongoDBPersistenceAdapter(namespace, pollDelayGenerator, database, mongoClient, serde);
  }
}
