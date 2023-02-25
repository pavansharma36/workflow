package io.github.pavansharma36.workflow.mongodb.adapter;

import com.mongodb.DBObjectCodecProvider;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import io.github.pavansharma36.workflow.api.WorkflowManager;
import io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter;
import io.github.pavansharma36.workflow.api.adapter.base.BasePersistenceAdapter;
import io.github.pavansharma36.workflow.api.bean.id.ManagerId;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import io.github.pavansharma36.workflow.api.executor.ExecutableTask;
import io.github.pavansharma36.workflow.api.executor.ExecutionResult;
import io.github.pavansharma36.workflow.api.model.ManagerInfo;
import io.github.pavansharma36.workflow.api.model.RunInfo;
import io.github.pavansharma36.workflow.api.model.TaskInfo;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import io.github.pavansharma36.workflow.mongodb.helper.JacksonCodecProvider;
import io.github.pavansharma36.workflow.mongodb.helper.MongoDBQueryHelper;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

public class MongoDBPersistenceAdapter extends BasePersistenceAdapter implements PersistenceAdapter {

  private static final CodecRegistry CODEC = new JacksonCodecProvider(CodecRegistries.fromProviders(
      Arrays.asList(new ValueCodecProvider(), new BsonValueCodecProvider(), new DBObjectCodecProvider())));

  private final String database;
  private final MongoClient mongoClient;

  protected MongoDBPersistenceAdapter(String namespace, PollDelayGenerator pollDelayGenerator,
                                      Serde serde, String database, MongoClient mongoClient) {
    super(namespace, pollDelayGenerator, serde);
    this.database = database;
    this.mongoClient = mongoClient;
  }

  @Override
  public void start(WorkflowManager workflowManager) {

  }

  @Override
  public void stop() {

  }

  @Override
  public boolean createOrUpdateManagerInfo(ManagerInfo managerInfo) {
    Bson filter = Filters.eq(MongoDBQueryHelper.ManagerInfo.MANAGER_ID_KEY,
        new BsonString(managerInfo.getManagerId().getId()));
    BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
    CODEC.get(ManagerInfo.class).encode(writer, managerInfo, null);
    Bson update = writer.getDocument();
    UpdateOptions options = new UpdateOptions().upsert(true);
    return collection(MongoDBQueryHelper.ManagerInfo.collectionName(namespace))
        .updateOne(filter, update, options).wasAcknowledged();
  }

  @Override
  public List<ManagerInfo> getAllManagerInfos() {
    try (MongoCursor<ManagerInfo> infos = collection(MongoDBQueryHelper.ManagerInfo.collectionName(namespace))
        .find(ManagerInfo.class).cursor()) {
      List<ManagerInfo> docs = new LinkedList<>();
      while (infos.hasNext()) {
        docs.add(infos.next());
      }
      return docs;
    }
  }

  @Override
  public boolean removeManagerInfo(ManagerId id) {
    Bson filter = Filters.eq(MongoDBQueryHelper.ManagerInfo.MANAGER_ID_KEY,
        new BsonString(id.getId()));
    return collection(MongoDBQueryHelper.ManagerInfo.collectionName(namespace)).deleteOne(filter)
        .wasAcknowledged();
  }

  @Override
  public boolean updateQueuedTime(RunId runId, TaskId taskId) {
    Bson filter = Filters.and(Filters.eq(MongoDBQueryHelper.TaskInfo.RUN_ID_KEY, runId.getId()),
        Filters.eq(MongoDBQueryHelper.TaskInfo.TASK_ID_KEY, taskId.getId()));
    Bson update = Updates.set(MongoDBQueryHelper.TaskInfo.QUEUED_TIME_KEY, System.currentTimeMillis());
    return collection(MongoDBQueryHelper.TaskInfo.collectionName(namespace)).updateOne(filter, update)
        .wasAcknowledged();
  }

  @Override
  public boolean updateStartTime(RunId runId) {
    return false;
  }

  @Override
  public boolean updateStartTime(RunId runId, TaskId taskId, ManagerId processedBy) {
    return false;
  }

  @Override
  public boolean completeTask(ExecutableTask executableTask, ExecutionResult executionResult) {
    return false;
  }

  @Override
  public Optional<TaskInfo> getTaskInfo(RunId runId, TaskId taskId) {
    return Optional.empty();
  }

  @Override
  public Optional<RunInfo> getRunInfo(RunId runId) {
    return Optional.empty();
  }

  @Override
  public void createRunInfo(RunInfo runInfo) {

  }

  @Override
  public boolean updateRunInfoEpoch(RunId runId) {
    return false;
  }

  @Override
  public void createTaskInfos(RunId runId, List<TaskInfo> taskInfos) {

  }

  @Override
  public boolean cleanup(RunId runId) {
    return false;
  }

  @Override
  public List<RunInfo> getStuckRunInfos(Duration maxDuration) {
    return null;
  }

  @Override
  public PollDelayGenerator heartbeatDelayGenerator() {
    return null;
  }

  private MongoCollection<Document> collection(String collection) {
    return mongoClient.getDatabase(database).getCollection(collection).withCodecRegistry(
        new JacksonCodecProvider(mongoClient.getDatabase(database).getCodecRegistry()));
  }

}
