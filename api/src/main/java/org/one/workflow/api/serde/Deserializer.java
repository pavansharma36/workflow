package org.one.workflow.api.serde;

public interface Deserializer {

  <T> T deserialize(byte[] bytes, Class<T> clazz);

}
