package io.github.pavansharma36.workflow.api.adapter;

import java.util.Optional;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.task.TaskType;
import io.github.pavansharma36.workflow.api.executor.ExecutableTask;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;

/**
 * Api to interact with queueing system (eg. redis, mongodb)
 */
public interface QueueAdapter extends Adapter {

  /**
   * {@link PollDelayGenerator} for polling queue.
   *
   * @return - {@link PollDelayGenerator}
   */
  PollDelayGenerator pollDelayGenerator();

  /**
   * Push given executable task into queue for execution.
   *
   * @param task - task.
   */
  void pushTask(ExecutableTask task);

  /**
   * Poll and get task to process from queue.
   *
   * @param taskType - taskType
   * @return - task from queue. empty otherwise
   */
  Optional<ExecutableTask> pollTask(TaskType taskType);

  /**
   * When task processing is completed. commit processed task.
   *
   * @param task - completed task.
   * @return - true if committed.
   */
  boolean commitTaskProcessed(ExecutableTask task);

  /**
   * Push given run id as updated to schedule next tasks.
   *
   * @param runId - runId
   */
  void pushUpdatedRun(RunId runId);

  /**
   * Poll and get updated runId to process from queue.
   *
   * @return - runId which was updated.
   */
  Optional<RunId> pollUpdatedRun();

  /**
   * Commit updated run id process.
   *
   * @param runId - commit run updated.
   * @return - true if commited.
   */
  boolean commitUpdatedRunProcess(RunId runId);

}
