package io.github.pavansharma36.workflow.api.serde;

/**
 * Deserialize api to convert byte array to class.
 */
public interface Deserializer {

  <T> T deserialize(byte[] bytes, Class<T> clazz);

}
