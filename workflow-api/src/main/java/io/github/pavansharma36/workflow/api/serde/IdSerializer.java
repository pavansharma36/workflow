package io.github.pavansharma36.workflow.api.serde;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.pavansharma36.workflow.api.bean.id.Id;
import java.io.IOException;

/**
 * jackson serializer for Id.
 */
public class IdSerializer extends JsonSerializer<Id> {

  /**
   * serialize id as string.
   *
   * @param id - id to serialize
   * @param jsonGenerator - json
   * @param serializerProvider - serializer
   * @throws IOException - invalid
   */
  @Override
  public void serialize(Id id, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeString(id.getId());
  }
}
