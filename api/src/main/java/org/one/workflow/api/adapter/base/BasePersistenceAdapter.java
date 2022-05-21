package org.one.workflow.api.adapter.base;

import org.one.workflow.api.adapter.PersistenceAdapter;
import org.one.workflow.api.serde.Deserializer;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.serde.Serializer;
import org.one.workflow.api.util.PollDelayGenerator;

/**
 * Base class for all {@link org.one.workflow.api.adapter.PersistenceAdapter}.
 */
public abstract class BasePersistenceAdapter extends BaseAdapter implements PersistenceAdapter {

  protected final Serializer serializer;
  protected final Deserializer deserializer;

  protected BasePersistenceAdapter(String namespace,
                                PollDelayGenerator pollDelayGenerator,
                                Serde serde) {
    super(namespace, pollDelayGenerator, serde);
    this.serializer = serde.serializer();
    this.deserializer = serde.deserializer();
  }
}
