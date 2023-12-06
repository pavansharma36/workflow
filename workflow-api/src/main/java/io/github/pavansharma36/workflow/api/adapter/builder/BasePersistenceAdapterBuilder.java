package io.github.pavansharma36.workflow.api.adapter.builder;

import io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;

public abstract class BasePersistenceAdapterBuilder<T extends BasePersistenceAdapterBuilder<T>>
    extends BaseAdapterBuilder<T, PersistenceAdapter> {

  public T withHeartbeatDelayGenerator(PollDelayGenerator heartbeatDelayGenerator) {
    this.pollDelayGenerator = heartbeatDelayGenerator;
    return (T) this;
  }
}
