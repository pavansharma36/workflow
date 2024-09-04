package io.github.pavansharma36.workflow.api.adapter.base;

import io.github.pavansharma36.workflow.api.adapter.Adapter;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Base class for all implmetation of {@link Adapter}.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseAdapter implements Adapter {

  protected final String namespace;

  /**
   * delay generator can have use according to type of adapter.
   * eg. poll delay for queue and scheduler.
   *     heartbeat for persistence adapter.
   */
  protected final PollDelayGenerator pollDelayGenerator;

}
