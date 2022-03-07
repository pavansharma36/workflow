package org.one.workflow.api;

import java.io.Closeable;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.executor.ExecutionResult;

public interface WorkflowManager extends Closeable {

  void start();

  String workflowManagerId();

  RunId submit(Task root);

  RunId submit(RunId runId, Task root);

  boolean completeAsyncTask(RunId runId, TaskId taskId, ExecutionResult executionResult);

  boolean cancelRun(RunId runId);

  Optional<ExecutionResult> getTaskExecutionResult(RunId runId, TaskId taskId);

  ExecutorService executorService();

  ScheduledExecutorService scheduledExecutorService();

  WorkflowManagerListener workflowManagerListener();

}
