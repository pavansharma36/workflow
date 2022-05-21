package org.one.workflow.api.adapter.base;

import org.one.workflow.api.adapter.QueueAdapter;
import org.one.workflow.api.serde.Deserializer;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.serde.Serializer;
import org.one.workflow.api.util.PollDelayGenerator;

/**
 * Base class for all {@link QueueAdapter}.
 */
public abstract class BaseQueueAdapter extends BaseAdapter implements QueueAdapter {

  protected final Serializer serializer;
  protected final Deserializer deserializer;

  protected BaseQueueAdapter(String namespace,
                          PollDelayGenerator pollDelayGenerator,
                          Serde serde) {
    super(namespace, pollDelayGenerator, serde);
    this.serializer = serde.serializer();
    this.deserializer = serde.deserializer();
  }
}
