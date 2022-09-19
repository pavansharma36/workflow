package io.github.pavansharma36.workflow.api.adapter.builder;

import io.github.pavansharma36.workflow.api.adapter.ScheduleAdapter;
import java.time.Duration;
import lombok.NonNull;
import io.github.pavansharma36.workflow.api.util.FixedPollDelayGenerator;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;

/**
 * Base class for all schedule adapter builder.
 *
 * @param <S> - type of schedule adapter builder.
 */
public abstract class BaseScheduleAdapterBuilder<S extends BaseScheduleAdapterBuilder<S>>
    extends BaseAdapterBuilder<S, ScheduleAdapter> {

  protected PollDelayGenerator maintenanceDelayGenerator = new FixedPollDelayGenerator(
      Duration.ofHours(1L));
  protected Duration maxRunDuration = Duration.ofDays(7L);

  public BaseScheduleAdapterBuilder<S> withMaintenanceDelayGenerator(
      @NonNull final PollDelayGenerator maintenanceDelayGenerator) {
    this.maintenanceDelayGenerator = maintenanceDelayGenerator;
    return this;
  }

  public BaseScheduleAdapterBuilder<S> maxRunDuration(@NonNull Duration maxRunDuration) {
    this.maxRunDuration = maxRunDuration;
    return this;
  }

}
