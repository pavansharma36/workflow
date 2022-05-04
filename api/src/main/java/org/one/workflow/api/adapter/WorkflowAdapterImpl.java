package org.one.workflow.api.adapter;

import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.bean.State;
import org.one.workflow.api.util.WorkflowException;

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

}
