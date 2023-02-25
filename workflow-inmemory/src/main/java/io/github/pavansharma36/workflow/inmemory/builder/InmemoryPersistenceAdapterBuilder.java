package io.github.pavansharma36.workflow.inmemory.builder;

import io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.BasePersistenceAdapterBuilder;
import io.github.pavansharma36.workflow.inmemory.adapter.InmemoryPersistenceAdapter;

public class InmemoryPersistenceAdapterBuilder
    extends BasePersistenceAdapterBuilder<InmemoryPersistenceAdapterBuilder> {
  @Override
  public PersistenceAdapter build() {
    return new InmemoryPersistenceAdapter(namespace, pollDelayGenerator);
  }
}
