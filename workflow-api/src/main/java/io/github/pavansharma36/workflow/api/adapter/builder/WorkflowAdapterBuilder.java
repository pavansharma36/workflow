package io.github.pavansharma36.workflow.api.adapter.builder;

import io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter;
import io.github.pavansharma36.workflow.api.adapter.QueueAdapter;
import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.adapter.impl.WorkflowAdapterImpl;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import java.time.Duration;

/**
 * Base class for all adapter builder.
 */
public class WorkflowAdapterBuilder {

  protected BaseScheduleAdapterBuilder<?> scheduleAdapterBuilder;
  protected BasePersistenceAdapterBuilder<?> persistenceAdapterBuilder;
  protected BaseAdapterBuilder<?, ? extends QueueAdapter> queueAdapterBuilder;

  public WorkflowAdapterBuilder withQueuePollDelayGenerator(
      final PollDelayGenerator pollDelayGenerator) {
    this.queueAdapterBuilder.withPollDelayGenerator(pollDelayGenerator);
    return this;
  }

  /**
   * schedule poll delay generator to use.
   *
   * @param pollDelayGenerator - generator
   * @return - this
   */
  public WorkflowAdapterBuilder withSchedulePollDelayGenerator(
      final PollDelayGenerator pollDelayGenerator) {
    this.scheduleAdapterBuilder.withPollDelayGenerator(pollDelayGenerator);
    return this;
  }

  /**
   * heartbeat generator to use.
   *
   * @param heartbeatDelayGenerator - generator
   * @return - this
   */
  public WorkflowAdapterBuilder withHeartbeatDelayGenerator(PollDelayGenerator
                                                                heartbeatDelayGenerator) {
    this.persistenceAdapterBuilder.withHeartbeatDelayGenerator(heartbeatDelayGenerator);
    return this;
  }

  /**
   * poll delay generator to use.
   *
   * @param pollDelayGenerator - generator
   * @return - this
   */
  public WorkflowAdapterBuilder withMaintenancePollDelayGenerator(
      final PollDelayGenerator pollDelayGenerator
  ) {
    this.scheduleAdapterBuilder.withMaintenanceDelayGenerator(pollDelayGenerator);
    return this;
  }

  /**
   * max run duration to use.
   *
   * @param duration - duration
   * @return - this
   */
  public WorkflowAdapterBuilder withMaxRunDuration(Duration duration) {
    this.scheduleAdapterBuilder.maxRunDuration(duration);
    return this;
  }

  /**
   * scheduler adapter to use.
   *
   * @param scheduleAdapterBuilder - adapter
   * @return - this
   */
  public WorkflowAdapterBuilder withScheduleAdapterBuilder(BaseScheduleAdapterBuilder<?>
                                                               scheduleAdapterBuilder) {
    this.scheduleAdapterBuilder = scheduleAdapterBuilder;
    return this;
  }

  /**
   * persistent adapter to use.
   *
   * @param persistenceAdapterBuilder - adaper
   * @return - this
   */
  public WorkflowAdapterBuilder withPersistenceAdapterBuilder(BasePersistenceAdapterBuilder<?>
                                                                  persistenceAdapterBuilder) {
    this.persistenceAdapterBuilder = persistenceAdapterBuilder;
    return this;
  }

  /**
   * queue adapter to use.
   *
   * @param queueAdapterBuilder - queue adaper
   * @return - this
   */
  public WorkflowAdapterBuilder withQueueAdapterBuilder(
      BaseAdapterBuilder<?, ? extends QueueAdapter> queueAdapterBuilder) {
    this.queueAdapterBuilder = queueAdapterBuilder;
    return this;
  }

  /**
   * build {@link WorkflowAdapter}.
   *
   * @return - instance of workflowadaper.
   */
  public WorkflowAdapter build() {
    scheduleAdapterBuilder.validate();
    queueAdapterBuilder.validate();
    persistenceAdapterBuilder.validate();

    return new WorkflowAdapterImpl(scheduleAdapterBuilder.build(),
        queueAdapterBuilder.build(),
        persistenceAdapterBuilder.build());
  }

}
