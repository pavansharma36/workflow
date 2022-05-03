package org.one.workflow.api.serde;

/**
 * serializer and deserializer provider api.
 */
public interface Serde {

  Serializer serializer();

  Deserializer deserializer();

}
