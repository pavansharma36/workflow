package io.github.pavansharma36.workflow.inmemory.builder;

import io.github.pavansharma36.workflow.api.adapter.QueueAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.BaseAdapterBuilder;
import io.github.pavansharma36.workflow.api.util.WorkflowException;
import io.github.pavansharma36.workflow.inmemory.adapter.InmemoryQueueAdapter;

/**
 * queue adapter builder for inmemory workflow.
 */
public class InmemoryQueueAdapterBuilder
    extends BaseAdapterBuilder<InmemoryQueueAdapterBuilder, QueueAdapter> {

  /**
   * default constructor.
   */
  public InmemoryQueueAdapterBuilder() {
    namespace = "NA";
  }

  @Override
  public QueueAdapter build() {
    return new InmemoryQueueAdapter(pollDelayGenerator);
  }

  @Override
  public InmemoryQueueAdapterBuilder withNamespace(String namespace) {
    throw new WorkflowException("Namespace is not supported for inmemory");
  }
}
