package io.github.pavansharma36.workflow.api.serde;

/**
 * Serializer api to convert object to byte array.
 */
public interface Serializer {

  byte[] serialize(Object o);

}
