package io.github.pavansharma36.workflow.mongodb.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

public class JacksonCodec<T> implements Codec<T> {

  private final ObjectMapper bsonObjectMapper;
  private final Codec<RawBsonDocument> rawBsonDocumentCodec;
  private final Class<T> type;

  public JacksonCodec(ObjectMapper bsonObjectMapper,
                      CodecRegistry codecRegistry,
                      Class<T> type) {
    this.bsonObjectMapper = bsonObjectMapper;
    this.rawBsonDocumentCodec = codecRegistry.get(RawBsonDocument.class);
    this.type = type;
  }
  @Override
  public T decode(BsonReader bsonReader, DecoderContext decoderContext) {
    try {
      RawBsonDocument document = rawBsonDocumentCodec.decode(bsonReader, decoderContext);
      return bsonObjectMapper.readValue(document.getByteBuffer().array(), type);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void encode(BsonWriter bsonWriter, T t, EncoderContext encoderContext) {
    try {
      byte[] data = bsonObjectMapper.writeValueAsBytes(t);
      rawBsonDocumentCodec.encode(bsonWriter, new RawBsonDocument(data), encoderContext);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Class<T> getEncoderClass() {
    return type;
  }
}
