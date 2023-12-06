package io.github.pavansharma36.workflow.inmemory.builder;

import io.github.pavansharma36.workflow.api.adapter.ScheduleAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.BaseScheduleAdapterBuilder;
import io.github.pavansharma36.workflow.api.util.WorkflowException;
import io.github.pavansharma36.workflow.inmemory.adapter.InmemorySchedulerAdapter;

public class InmemoryScheduleAdapterBuilder extends BaseScheduleAdapterBuilder<InmemoryScheduleAdapterBuilder> {

  public InmemoryScheduleAdapterBuilder() {
    namespace = "NA";
  }

  @Override
  public ScheduleAdapter build() {
    return new InmemorySchedulerAdapter(pollDelayGenerator, maintenanceDelayGenerator);
  }

  @Override
  public InmemoryScheduleAdapterBuilder withNamespace(String namespace) {
    throw new WorkflowException("Namespace is not supported for inmemory");
  }
}
