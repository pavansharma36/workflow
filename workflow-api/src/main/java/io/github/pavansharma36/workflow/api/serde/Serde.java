package io.github.pavansharma36.workflow.api.serde;

/**
 * serializer and deserializer provider api.
 */
public interface Serde {

  Serializer serializer();

  Deserializer deserializer();

}
