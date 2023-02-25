package io.github.pavansharma36.workflow.inmemory.builder;

import io.github.pavansharma36.workflow.api.adapter.QueueAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.BaseAdapterBuilder;
import io.github.pavansharma36.workflow.inmemory.adapter.InmemoryQueueAdapter;

public class InmemoryQueueAdapterBuilder
    extends BaseAdapterBuilder<InmemoryQueueAdapterBuilder, QueueAdapter> {
  @Override
  public QueueAdapter build() {
    return new InmemoryQueueAdapter(pollDelayGenerator);
  }
}
