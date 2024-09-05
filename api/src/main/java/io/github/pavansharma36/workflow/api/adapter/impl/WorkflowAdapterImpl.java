package io.github.pavansharma36.workflow.api.adapter.impl;

import io.github.pavansharma36.workflow.api.WorkflowManager;
import io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter;
import io.github.pavansharma36.workflow.api.adapter.QueueAdapter;
import io.github.pavansharma36.workflow.api.adapter.ScheduleAdapter;
import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.bean.State;
import io.github.pavansharma36.workflow.api.util.WorkflowException;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;

/**
 * Simple implementation of {@link WorkflowAdapter}.
 */
@RequiredArgsConstructor
public class WorkflowAdapterImpl implements WorkflowAdapter {

  private final AtomicReference<State> state = new AtomicReference<>(State.INIT);

  private final ScheduleAdapter scheduleAdapter;
  private final QueueAdapter queueAdapter;
  private final PersistenceAdapter persistenceAdapter;

  @Override
  public void start(final WorkflowManager workflowManager) {
    if (state.compareAndSet(State.INIT, State.STARTING)) {
      persistenceAdapter.start(workflowManager);
      scheduleAdapter.start(workflowManager);
      queueAdapter.start(workflowManager);

      state.compareAndSet(State.STARTING, State.STARTED);
    } else {
      throw new WorkflowException("Not valid state " + state.get());
    }
  }

  @Override
  public void stop() {
    if (state.compareAndSet(State.STARTED, State.STOPPING)) {
      queueAdapter.stop();
      scheduleAdapter.stop();
      persistenceAdapter.stop();

      state.compareAndSet(State.STOPPING, State.STOPPED);
    } else {
      throw new WorkflowException("Not valid state " + state.get());
    }
  }

  @Override
  public ScheduleAdapter scheduleAdapter() {
    return scheduleAdapter;
  }

  @Override
  public PersistenceAdapter persistenceAdapter() {
    return persistenceAdapter;
  }

  @Override
  public QueueAdapter queueAdapter() {
    return queueAdapter;
  }

  @Override
  public void maintenance() {
    scheduleAdapter().maintenance(this);
    persistenceAdapter().maintenance(this);
    queueAdapter().maintenance(this);
  }
}
