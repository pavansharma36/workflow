package org.one.workflow.api.adapter.builder;

import java.time.Duration;
import org.one.workflow.api.adapter.PersistenceAdapter;
import org.one.workflow.api.adapter.QueueAdapter;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.adapter.impl.WorkflowAdapterImpl;
import org.one.workflow.api.util.PollDelayGenerator;

/**
 * Base class for all adapter builder.
 */
public class WorkflowAdapterBuilder<S extends WorkflowAdapterBuilder<S, S1, S2, S3>,
    S1 extends BaseScheduleAdapterBuilder<S1>,
    S2 extends BaseAdapterBuilder<S2, PersistenceAdapter>,
    S3 extends BaseAdapterBuilder<S3, QueueAdapter>> {

  protected S1 scheduleAdapterBuilder;
  protected S2 persistenceAdapterBuilder;
  protected S3 queueAdapterBuilder;

  public WorkflowAdapterBuilder<S, S1, S2, S3> withQueuePollDelayGenerator(
      final PollDelayGenerator pollDelayGenerator) {
    this.queueAdapterBuilder.withPollDelayGenerator(pollDelayGenerator);
    return this;
  }

  public WorkflowAdapterBuilder<S, S1, S2, S3> withSchedulePollDelayGenerator(
      final PollDelayGenerator pollDelayGenerator) {
    this.scheduleAdapterBuilder.withPollDelayGenerator(pollDelayGenerator);
    return this;
  }

  public WorkflowAdapterBuilder<S, S1, S2, S3> withMaintenancePollDelayGenerator(
      final PollDelayGenerator pollDelayGenerator
  ) {
    this.scheduleAdapterBuilder.withMaintenanceDelayGenerator(pollDelayGenerator);
    return this;
  }

  public WorkflowAdapterBuilder<S, S1, S2, S3> withMaxRunDuration(Duration duration) {
    this.scheduleAdapterBuilder.maxRunDuration(duration);
    return this;
  }

  /**
   * build {@link WorkflowAdapter}.
   *
   * @return - instance of workflowadaper.
   */
  public WorkflowAdapter build() {
    return new WorkflowAdapterImpl(scheduleAdapterBuilder.build(),
        queueAdapterBuilder.build(),
        persistenceAdapterBuilder.build());
  }

}
