package io.github.pavansharma36.workflow.api;

import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import io.github.pavansharma36.workflow.api.bean.task.Task;
import io.github.pavansharma36.workflow.api.executor.ExecutionResult;
import io.github.pavansharma36.workflow.api.model.ManagerInfo;
import java.io.Closeable;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Main api to interact with workflow manager.
 * Eg. submit dags. get run info. get execution result.
 */
public interface WorkflowManager extends Closeable {

  /**
   * start workflow manager with given task types it will start processing tasks.
   */
  void start();

  /**
   * info of this workflow manager.
   *
   * @return - info.
   */
  ManagerInfo info();

  /**
   * Submit dag to process.
   *
   * @param root - root task.
   * @return - generated run id of submitted dag.
   */
  RunId submit(Task root);

  /**
   * Submit dag to process with given run id.
   *
   * @param runId - unique run id for this dag.
   * @param root - root task of this dag,
   * @return - runId.
   */
  RunId submit(RunId runId, Task root);

  /**
   * When async tasks starts dag execution will not continue until completed externally.
   *
   * @param runId - runId
   * @param taskId - taskId.
   * @param executionResult - result of async task.
   * @return - true if completed. or false if failed to complete.
   */
  boolean completeAsyncTask(RunId runId, TaskId taskId, ExecutionResult executionResult);

  /**
   * Attempt to cancel given runId.
   * All running task will continue to run. pending tasks will not execute.
   * Event with ABORTED status will be published.
   *
   * @param runId - runId of dag.
   * @return - true of cancelled.
   */
  boolean cancelRun(RunId runId);

  /**
   * return result of given task.
   *
   * @param runId - runId.
   * @param taskId - taskId.
   * @return - result.
   */
  Optional<ExecutionResult> getTaskExecutionResult(RunId runId, TaskId taskId);

  ExecutorService executorService();

  ScheduledExecutorService scheduledExecutorService();

  /**
   * Workflow manager listener.
   * Add hooks for various dag and task events.
   *
   * @return - {@link WorkflowManagerListener}.
   */
  WorkflowManagerListener workflowManagerListener();

}
