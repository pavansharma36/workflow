package io.github.pavansharma36.workflow.inmemory.builder;

import io.github.pavansharma36.workflow.api.adapter.builder.WorkflowAdapterBuilder;

public class InmemoryWorkflowAdapterBuilder
    extends WorkflowAdapterBuilder {


  public static InmemoryWorkflowAdapterBuilder builder() {
    InmemoryWorkflowAdapterBuilder builder = new InmemoryWorkflowAdapterBuilder();
    builder.scheduleAdapterBuilder = new InmemoryScheduleAdapterBuilder();
    builder.queueAdapterBuilder = new InmemoryQueueAdapterBuilder();
    builder.persistenceAdapterBuilder = new InmemoryPersistenceAdapterBuilder();
    return builder;
  }
}
