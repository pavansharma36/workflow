package io.github.pavansharma36.workflow.mongodb.migration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import io.github.pavansharma36.workflow.mongodb.helper.MongoDbQueryHelper;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;

/**
 * migration to create indexes in mongo for mongopersistenceadapter.
 */
@RequiredArgsConstructor
public class PersistenceMigration implements Runnable {

  private final String namespace;
  private final String database;
  private final MongoClient client;

  @Override
  public void run() {
    init();
  }

  private MongoCollection<Document> collection(String collection) {
    return client.getDatabase(database).getCollection(collection);
  }

  private void init() {
    managerInfo();
    taskInfo();
    runInfo();
  }

  private void managerInfo() {
    BsonDocument index = new BsonDocument()
        .append(MongoDbQueryHelper.ManagerInfo.MANAGER_ID_KEY, new BsonInt32(1));
    IndexOptions options = new IndexOptions().background(true)
        .name("manager_info_id_unique_idx").unique(true);
    collection(MongoDbQueryHelper.ManagerInfo.collectionName(namespace))
        .createIndex(index, options);
  }

  private void taskInfo() {
    List<IndexModel> indexes = new LinkedList<>();

    indexes.add(new IndexModel(new BsonDocument().append(
            MongoDbQueryHelper.TaskInfo.RUN_ID_KEY, new BsonInt32(1))
        .append(MongoDbQueryHelper.TaskInfo.TASK_ID_KEY, new BsonInt32(1)),
        new IndexOptions().background(true)
                .name("task_info_run_id_task_id_unique_idx").unique(true)));

    collection(MongoDbQueryHelper.TaskInfo.collectionName(namespace))
        .createIndexes(indexes);
  }

  private void runInfo() {
    BsonDocument index = new BsonDocument()
        .append(MongoDbQueryHelper.RunInfo.RUN_ID_KEY, new BsonInt32(1));
    IndexOptions options = new IndexOptions().background(true)
        .name("run_info_id_unique_idx").unique(true);
    collection(MongoDbQueryHelper.RunInfo.collectionName(namespace))
        .createIndex(index, options);
  }
}
