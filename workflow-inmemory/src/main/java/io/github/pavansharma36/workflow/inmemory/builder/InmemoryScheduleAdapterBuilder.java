package io.github.pavansharma36.workflow.inmemory.builder;

import io.github.pavansharma36.workflow.api.adapter.ScheduleAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.BaseScheduleAdapterBuilder;
import io.github.pavansharma36.workflow.inmemory.adapter.InmemorySchedulerAdapter;

public class InmemoryScheduleAdapterBuilder extends BaseScheduleAdapterBuilder<InmemoryScheduleAdapterBuilder> {
  @Override
  public ScheduleAdapter build() {
    return new InmemorySchedulerAdapter(pollDelayGenerator, maintenanceDelayGenerator);
  }
}
