package io.github.pavansharma36.workflow.api.adapter.builder;

import io.github.pavansharma36.workflow.api.adapter.Adapter;
import io.github.pavansharma36.workflow.api.util.FixedPollDelayGenerator;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import io.github.pavansharma36.workflow.api.util.WorkflowException;
import java.time.Duration;

/**
 * Base class for all AdapterBuilders.
 *
 * @param <T> - type of extending class.
 */
public abstract class BaseAdapterBuilder<T extends BaseAdapterBuilder<T, A>, A extends Adapter> {

  protected String namespace;
  protected PollDelayGenerator pollDelayGenerator;


  public T withNamespace(String namespace) {
    this.namespace = namespace;
    return (T) this;
  }

  public T withPollDelayGenerator(PollDelayGenerator pollDelayGenerator) {
    this.pollDelayGenerator = pollDelayGenerator;
    return (T) this;
  }

  protected void validate() {
    if ((namespace == null) || namespace.isEmpty()) {
      throw new WorkflowException("Namespace cant be blank");
    }

    if (pollDelayGenerator == null) {
      pollDelayGenerator = new FixedPollDelayGenerator(Duration.ofSeconds(1L));
    }
  }

  public abstract A build();

}
