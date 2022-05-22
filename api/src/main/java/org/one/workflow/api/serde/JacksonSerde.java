package org.one.workflow.api.serde;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.one.workflow.api.util.WorkflowException;

/**
 * Serde implementation using jackson.
 */
@Getter
@AllArgsConstructor
public class JacksonSerde implements Serde {

  private final ObjectMapper mapper;

  /**
   * static method to get instance of {@link JacksonSerde}.
   *
   * @return - instance
   */
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
        throw new WorkflowException("Error while serializing " + e.getMessage(), e);
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
          throw new WorkflowException("Error while deserializing " + e.getMessage(), e);
        }
      }
    };
  }

}
