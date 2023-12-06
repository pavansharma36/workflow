package io.github.pavansharma36.workflow.inmemory.builder;

import io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.BasePersistenceAdapterBuilder;
import io.github.pavansharma36.workflow.api.util.WorkflowException;
import io.github.pavansharma36.workflow.inmemory.adapter.InmemoryPersistenceAdapter;

public class InmemoryPersistenceAdapterBuilder
    extends BasePersistenceAdapterBuilder<InmemoryPersistenceAdapterBuilder> {

  public InmemoryPersistenceAdapterBuilder() {
    namespace = "NA";
  }
  @Override
  public PersistenceAdapter build() {
    return new InmemoryPersistenceAdapter(namespace, pollDelayGenerator);
  }

  @Override
  public InmemoryPersistenceAdapterBuilder withNamespace(String namespace) {
    throw new WorkflowException("Namespace is not supported for inmemory");
  }

}
