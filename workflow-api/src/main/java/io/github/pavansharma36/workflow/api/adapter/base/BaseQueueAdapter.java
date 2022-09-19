package io.github.pavansharma36.workflow.api.adapter.base;

import io.github.pavansharma36.workflow.api.adapter.QueueAdapter;
import io.github.pavansharma36.workflow.api.serde.Deserializer;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.api.serde.Serializer;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;

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
