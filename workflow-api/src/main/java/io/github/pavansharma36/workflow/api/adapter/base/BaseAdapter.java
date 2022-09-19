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
  protected final PollDelayGenerator pollDelayGenerator;
  protected final Serde serde;

}
