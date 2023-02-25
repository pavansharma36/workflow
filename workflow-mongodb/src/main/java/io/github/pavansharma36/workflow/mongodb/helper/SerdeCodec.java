package io.github.pavansharma36.workflow.mongodb.helper;

import io.github.pavansharma36.workflow.api.serde.Deserializer;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.api.serde.Serializer;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

public class SerdeCodec<T> implements Codec<T> {

  private final Serializer serializer;
  private final Deserializer deserializer;
  private final Codec<RawBsonDocument> rawBsonDocumentCodec;
  private final Class<T> type;

  public SerdeCodec(Serde serde,
                    CodecRegistry codecRegistry,
                    Class<T> type) {
    this.serializer = serde.serializer();
    this.deserializer = serde.deserializer();
    this.rawBsonDocumentCodec = codecRegistry.get(RawBsonDocument.class);
    this.type = type;
  }
  @Override
  public T decode(BsonReader bsonReader, DecoderContext decoderContext) {
    RawBsonDocument document = rawBsonDocumentCodec.decode(bsonReader, decoderContext);
    return deserializer.deserialize(document.getByteBuffer().array(), type);
  }

  @Override
  public void encode(BsonWriter bsonWriter, T t, EncoderContext encoderContext) {
    byte[] data = serializer.serialize(t);
    rawBsonDocumentCodec.encode(bsonWriter, new RawBsonDocument(data), encoderContext);
  }

  @Override
  public Class<T> getEncoderClass() {
    return type;
  }
}
