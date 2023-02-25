package io.github.pavansharma36.workflow.mongodb.helper;

import io.github.pavansharma36.workflow.api.bean.id.ManagerId;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class IdCodecs {

  public static class ManagerIdCodec implements Codec<ManagerId> {
    @Override
    public ManagerId decode(BsonReader bsonReader, DecoderContext decoderContext) {
      return new ManagerId(bsonReader.readString());
    }

    @Override
    public void encode(BsonWriter bsonWriter, ManagerId managerId, EncoderContext encoderContext) {
      bsonWriter.writeString(managerId.getId());
    }

    @Override
    public Class<ManagerId> getEncoderClass() {
      return ManagerId.class;
    }
  }

  public static class RunIdCodec implements Codec<RunId> {

    @Override
    public RunId decode(BsonReader bsonReader, DecoderContext decoderContext) {
      return new RunId(bsonReader.readString());
    }

    @Override
    public void encode(BsonWriter bsonWriter, RunId runId, EncoderContext encoderContext) {
      bsonWriter.writeString(runId.getId());
    }

    @Override
    public Class<RunId> getEncoderClass() {
      return RunId.class;
    }
  }

  public static class TaskIdCodec  implements Codec<TaskId> {

    @Override
    public TaskId decode(BsonReader bsonReader, DecoderContext decoderContext) {
      return new TaskId(bsonReader.readString());
    }

    @Override
    public void encode(BsonWriter bsonWriter, TaskId taskId, EncoderContext encoderContext) {
      bsonWriter.writeString(taskId.getId());
    }

    @Override
    public Class<TaskId> getEncoderClass() {
      return TaskId.class;
    }
  }

}
