package io.github.pavansharma36.workflow.inmemory.builder;

import io.github.pavansharma36.workflow.api.adapter.builder.WorkflowAdapterBuilder;

/**
 * workflow adapter builder for inmemory workflow.
 */
public class InmemoryWorkflowAdapterBuilder
    extends WorkflowAdapterBuilder {

  /**
   * main builder.
   *
   * @return - this
   */
  public static InmemoryWorkflowAdapterBuilder builder() {
    InmemoryWorkflowAdapterBuilder builder = new InmemoryWorkflowAdapterBuilder();
    builder.scheduleAdapterBuilder = new InmemoryScheduleAdapterBuilder();
    builder.queueAdapterBuilder = new InmemoryQueueAdapterBuilder();
    builder.persistenceAdapterBuilder = new InmemoryPersistenceAdapterBuilder();
    return builder;
  }
}
