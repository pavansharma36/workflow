package org.one.workflow.api.adapter.base;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.one.workflow.api.adapter.Adapter;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.util.PollDelayGenerator;

/**
 * Base class for all implmetation of {@link org.one.workflow.api.adapter.Adapter}.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseAdapter implements Adapter {

  protected final String namespace;
  protected final PollDelayGenerator pollDelayGenerator;
  protected final Serde serde;

}
