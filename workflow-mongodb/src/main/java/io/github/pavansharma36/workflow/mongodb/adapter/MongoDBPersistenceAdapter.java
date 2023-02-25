package io.github.pavansharma36.workflow.mongodb.adapter;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

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
import io.github.pavansharma36.workflow.api.bean.task.TaskType;
import io.github.pavansharma36.workflow.api.dag.RunnableTaskDag;
import io.github.pavansharma36.workflow.api.executor.ExecutableTask;
import io.github.pavansharma36.workflow.api.executor.ExecutionResult;
import io.github.pavansharma36.workflow.api.model.ManagerInfo;
import io.github.pavansharma36.workflow.api.model.RunInfo;
import io.github.pavansharma36.workflow.api.model.TaskInfo;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import io.github.pavansharma36.workflow.mongodb.helper.IdCodecs;
import io.github.pavansharma36.workflow.mongodb.helper.MongoDBQueryHelper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

public class MongoDBPersistenceAdapter extends BasePersistenceAdapter implements PersistenceAdapter {

  private final String database;
  private final MongoClient mongoClient;

  private final CodecRegistry codec;

  public MongoDBPersistenceAdapter(String namespace, PollDelayGenerator heartbeatDelayGenerator,
                                      String database, MongoClient mongoClient, Serde serde) {
    super(namespace, heartbeatDelayGenerator);
    this.database = database;
    this.mongoClient = mongoClient;

    CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
        .register(ManagerInfo.class, RunInfo.class, TaskInfo.class, ExecutionResult.class, RunnableTaskDag.class,
            TaskType.class).build();
    CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
        fromProviders(pojoCodecProvider), fromCodecs(new IdCodecs.ManagerIdCodec(), new IdCodecs.RunIdCodec(), new IdCodecs.TaskIdCodec()));

    this.codec = pojoCodecRegistry;
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
    Bson update = Updates.combine(Updates.set(MongoDBQueryHelper.ManagerInfo.MANAGER_ID_KEY, managerInfo.getManagerId().getId()),
        Updates.set(MongoDBQueryHelper.ManagerInfo.START_TIME_KEY, managerInfo.getStartTimeEpoch()),
        Updates.set(MongoDBQueryHelper.ManagerInfo.HEARTBEAT_KEY, managerInfo.getHeartbeatEpoch()));
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
    Bson filter = Filters.eq(MongoDBQueryHelper.RunInfo.RUN_ID_KEY, runId.getId());
    Bson update = Updates.set(MongoDBQueryHelper.RunInfo.START_TIME_KEY, System.currentTimeMillis());
    return collection(MongoDBQueryHelper.RunInfo.collectionName(namespace)).updateOne(filter, update)
        .wasAcknowledged();
  }

  @Override
  public boolean updateStartTime(RunId runId, TaskId taskId, ManagerId processedBy) {
    Bson filter = Filters.and(Filters.eq(MongoDBQueryHelper.TaskInfo.RUN_ID_KEY, runId.getId()),
        Filters.eq(MongoDBQueryHelper.TaskInfo.TASK_ID_KEY, taskId.getId()));
    Bson update = Updates.combine(Updates.set(MongoDBQueryHelper.TaskInfo.START_TIME_KEY, System.currentTimeMillis()),
        Updates.set(MongoDBQueryHelper.TaskInfo.PROCESSED_BY_KEY, processedBy.getId()));
    return collection(MongoDBQueryHelper.TaskInfo.collectionName(namespace)).updateOne(filter, update)
        .wasAcknowledged();
  }

  @Override
  public boolean completeTask(ExecutableTask executableTask, ExecutionResult executionResult) {
    Bson filter = Filters.and(Filters.eq(MongoDBQueryHelper.TaskInfo.RUN_ID_KEY, executableTask.getRunId().getId()),
        Filters.eq(MongoDBQueryHelper.TaskInfo.TASK_ID_KEY, executableTask.getTaskId().getId()));
    Bson update = Updates.combine(Updates.set(MongoDBQueryHelper.TaskInfo.COMPLETION_TIME_KEY, System.currentTimeMillis()),
        Updates.set(MongoDBQueryHelper.TaskInfo.RESULT_KEY, executionResult));
    return collection(MongoDBQueryHelper.TaskInfo.collectionName(namespace)).updateOne(filter, update)
        .wasAcknowledged();
  }

  @Override
  public Optional<TaskInfo> getTaskInfo(RunId runId, TaskId taskId) {
    Bson filter = Filters.and(Filters.eq(MongoDBQueryHelper.TaskInfo.RUN_ID_KEY, runId.getId()),
        Filters.eq(MongoDBQueryHelper.TaskInfo.TASK_ID_KEY, taskId.getId()));
    TaskInfo taskInfo = collection(MongoDBQueryHelper.TaskInfo.collectionName(namespace))
        .find(filter, TaskInfo.class).first();
    return Optional.ofNullable(taskInfo);
  }

  @Override
  public Optional<RunInfo> getRunInfo(RunId runId) {
    Bson filter = Filters.eq(MongoDBQueryHelper.RunInfo.RUN_ID_KEY, runId.getId());
    RunInfo runInfo = collection(MongoDBQueryHelper.RunInfo.collectionName(namespace))
        .find(filter, RunInfo.class).first();
    return Optional.ofNullable(runInfo);
  }

  @Override
  public void createRunInfo(RunInfo runInfo) {
    BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
    codec.get(RunInfo.class).encode(writer, runInfo, EncoderContext.builder().build());
    BsonDocument doc = writer.getDocument();
    collection(MongoDBQueryHelper.RunInfo.collectionName(namespace))
        .insertOne(Document.parse(doc.toJson()));
  }

  @Override
  public boolean updateRunInfoEpoch(RunId runId) {
    Bson filter = Filters.eq(MongoDBQueryHelper.RunInfo.RUN_ID_KEY, runId.getId());
    Bson update = Updates.set(MongoDBQueryHelper.RunInfo.LAST_UPDATE_KEY, System.currentTimeMillis());
    return collection(MongoDBQueryHelper.RunInfo.collectionName(namespace)).updateOne(filter, update)
        .wasAcknowledged();
  }

  @Override
  public void createTaskInfos(RunId runId, List<TaskInfo> taskInfos) {
    List<Document> docs = new ArrayList<>(taskInfos.size());
    for (TaskInfo taskInfo : taskInfos) {
      taskInfo.setRunId(runId);
      BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
      codec.get(TaskInfo.class).encode(writer, taskInfo, EncoderContext.builder().build());
      BsonDocument doc = writer.getDocument();
      docs.add(Document.parse(doc.toJson()));
    }
    collection(MongoDBQueryHelper.TaskInfo.collectionName(namespace))
        .insertMany(docs);
  }

  @Override
  public boolean cleanup(RunId runId) {
    Bson taskFilter = Filters.eq(MongoDBQueryHelper.TaskInfo.RUN_ID_KEY, runId.getId());
    collection(MongoDBQueryHelper.TaskInfo.collectionName(namespace)).deleteMany(taskFilter);

    Bson runFilter = Filters.eq(MongoDBQueryHelper.RunInfo.RUN_ID_KEY, runId.getId());
    collection(MongoDBQueryHelper.RunInfo.collectionName(namespace)).deleteMany(runFilter);
    return true;
  }

  @Override
  public List<RunInfo> getStuckRunInfos(Duration maxDuration) {
    // TODO
    return Collections.emptyList();
  }

  private MongoCollection<Document> collection(String collection) {
    return mongoClient.getDatabase(database).getCollection(collection).withCodecRegistry(codec);
  }

}
