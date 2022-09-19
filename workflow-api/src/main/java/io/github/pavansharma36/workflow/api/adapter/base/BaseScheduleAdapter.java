package io.github.pavansharma36.workflow.api.adapter.base;

import io.github.pavansharma36.workflow.api.adapter.ScheduleAdapter;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;

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
