package io.github.pavansharma36.workflow.api.serde;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.pavansharma36.workflow.api.bean.id.Id;
import java.io.IOException;

public class IdSerializer extends StdSerializer<Id> {
  protected IdSerializer(Class<Id> t) {
    super(t);
  }

  @Override
  public void serialize(Id id, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeString(id.getId());
  }
}
