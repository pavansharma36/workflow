package org.one.workflow.api.adapter.base;

import org.one.workflow.api.adapter.ScheduleAdapter;
import org.one.workflow.api.serde.Serde;
import org.one.workflow.api.util.PollDelayGenerator;

/**
 * Base class for all {@link ScheduleAdapter}.
 */
public abstract class BaseScheduleAdapter extends BaseAdapter implements ScheduleAdapter {

  protected BaseScheduleAdapter(String namespace,
                             PollDelayGenerator pollDelayGenerator,
                             Serde serde) {
    super(namespace, pollDelayGenerator, serde);
  }
}
