package io.github.pavansharma36.workflow.api.adapter.builder;

import io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;

/**
 * persistent adapter builder.
 *
 * @param <T> - same class.
 */
public abstract class BasePersistenceAdapterBuilder<T extends BasePersistenceAdapterBuilder<T>>
    extends BaseAdapterBuilder<T, PersistenceAdapter> {

  /**
   * heartbeat delay generator to use.
   *
   * @param heartbeatDelayGenerator - generator
   * @return - this
   */
  public T withHeartbeatDelayGenerator(PollDelayGenerator heartbeatDelayGenerator) {
    this.pollDelayGenerator = heartbeatDelayGenerator;
    return (T) this;
  }
}
