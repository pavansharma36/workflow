package org.one.workflow.api.serde;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JacksonSerde implements Serde {

  private final ObjectMapper mapper;

  public static JacksonSerde getInstance() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    return new JacksonSerde(mapper);
  }

  @Override
  public Serializer serializer() {
    return o -> {
      try {
        return mapper.writeValueAsBytes(o);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Error while serializing " + e.getMessage(), e);
      }
    };
  }

  @Override
  public Deserializer deserializer() {
    return new Deserializer() {
      @Override
      public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
          return mapper.readValue(bytes, clazz);
        } catch (IOException e) {
          throw new RuntimeException("Error while deserializing " + e.getMessage(), e);
        }
      }
    };
  }

}
