package org.one.workflow.api.impl;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.one.workflow.api.WorkflowListener;
import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.WorkflowManagerListener;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.bean.RunEvent;
import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.dag.RunnableTaskDagBuilder;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.executor.TaskExecutor;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;
import org.one.workflow.api.queue.QueueConsumer;
import org.one.workflow.api.schedule.Scheduler;
import org.one.workflow.api.util.Utils;
import org.one.workflow.api.util.WorkflowException;

@Slf4j
public class WorkflowManagerImpl implements WorkflowManager {

  private final String runId = Utils.random();
  private final WorkflowManagerListener workflowManagerListener = new WorkflowManagerListener();
  private final WorkflowAdapter adapter;
  private final ExecutorService executorService;
  private final ScheduledExecutorService scheduledExecutorService;
  private final Scheduler scheduler;
  private final QueueConsumer queueConsumer;
  private final Duration maxRunDuration = Duration.ofDays(7L);

  protected WorkflowManagerImpl(final WorkflowAdapter adapter,
                                final ExecutorService executorService,
                                final ScheduledExecutorService scheduledExecutorService,
                                final List<TaskDefination> taskDefinations) {
    super();
    this.adapter = adapter;
    this.executorService = executorService;
    this.scheduledExecutorService = scheduledExecutorService;
    this.scheduler = new Scheduler(adapter);
    this.queueConsumer = new QueueConsumerImpl(adapter, taskDefinations);
  }

  @Override
  public void close() throws IOException {
    scheduler.stop();
    adapter.stop();
  }

  @Override
  public void start() {
    log.info("Starting workflow");
    adapter.start(this);

    scheduler.start(this);

    queueConsumer.start(this);

    // start maintenance task
    scheduledExecutorService().scheduleWithFixedDelay(() -> {
      log.info("Clearing all stuck workflows");
      List<RunInfo> stuckRuns = adapter.persistenceAdapter().getStuckRunInfos(maxRunDuration);
      stuckRuns.forEach(r -> {
        log.warn("Run {} stuck for more than {}, aborting now", r.getRunId(), maxRunDuration);
        cancelRun(r.getRunId());
      });
    }, 1, 1, TimeUnit.HOURS);
  }

  @Override
  public RunId submit(final Task root) {
    return submit(new RunId(), root);
  }

  @Override
  public RunId submit(final RunId runId, final Task root) {
    log.info("Submitting run {}", runId);
    final RunnableTaskDagBuilder builder = new RunnableTaskDagBuilder(root);

    final RunInfo runInfo = new RunInfo();
    runInfo.setRunId(runId);
    runInfo.setQueuedTime(System.currentTimeMillis());
    runInfo.setDag(builder.getEntries());
    runInfo.setLastUpdateEpoch(System.currentTimeMillis());

    adapter.persistenceAdapter().createRunInfo(runInfo);

    adapter.persistenceAdapter().createTaskInfos(runId,
        builder.getTasks().values().stream().map(t -> new TaskInfo(runId, t))
            .collect(Collectors.toList()));

    // for scheduler to pick.
    adapter.queueAdapter().pushUpdatedRun(runId);

    return runId;
  }

  @Override
  public ScheduledExecutorService scheduledExecutorService() {
    return scheduledExecutorService;
  }

  @Override
  public ExecutorService executorService() {
    return executorService;
  }

  @Override
  public boolean cancelRun(final RunId runId) {
    boolean cleanup = adapter.persistenceAdapter().cleanup(runId);
    if (cleanup) {
      workflowManagerListener().publishEvent(
          new RunEvent(runId, WorkflowListener.RunEventType.RUN_ABORTED));
    }
    return cleanup;
  }

  @Override
  public boolean completeAsyncTask(final RunId runId, final TaskId taskId,
                                   final ExecutionResult executionResult) {
    if ((executionResult == null) || (executionResult.getStatus() == null)) {
      throw new WorkflowException("Result cannot be null");
    }
    final Optional<TaskInfo> oTaskInfo = adapter.persistenceAdapter().getTaskInfo(runId, taskId);
    if (oTaskInfo.isPresent()) {
      adapter.persistenceAdapter()
          .completeTask(ExecutableTask.builder().runId(runId).taskId(taskId).build(),
              executionResult);
      adapter.queueAdapter().pushUpdatedRun(runId);
      return true;
    }
    return false;
  }

  @Override
  public Optional<ExecutionResult> getTaskExecutionResult(final RunId runId, final TaskId taskId) {
    return adapter.persistenceAdapter().getTaskInfo(runId, taskId).map(TaskInfo::getResult);
  }

  @Override
  public String workflowManagerId() {
    return runId;
  }

  @Override
  public WorkflowManagerListener workflowManagerListener() {
    return workflowManagerListener;
  }

  @Getter
  @Setter
  @Builder
  protected static class TaskDefination {
    private TaskType taskType;
    private int threads;
    private TaskExecutor taskExecutor;
    private ExecutorService executorService;
  }

}
