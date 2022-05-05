package org.one.workflow.api.adapter;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.one.workflow.api.WorkflowManagerLifecycle;
import org.one.workflow.api.bean.id.ManagerId;
import org.one.workflow.api.bean.id.RunId;
import org.one.workflow.api.bean.id.TaskId;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.model.ManagerInfo;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;
import org.one.workflow.api.util.PollDelayGenerator;

/**
 * Api to interact with underlying datastore (eg. redis, mongodb, postgres)
 */
public interface PersistenceAdapter extends Adapter {

  /**
   * create new manager info in data store or update heartbeat if present.
   *
   * @param managerInfo - WorkflowManager instance.
   * @return - true if created, false if updated.
   */
  boolean createOrUpdateManagerInfo(ManagerInfo managerInfo);

  /**
   * return list of all workflow managers in data store.
   *
   * @return - list of workflow managers running.
   */
  List<ManagerInfo> getAllManagerInfos();

  /**
   * removes manager info details from datastore.
   *
   * @param id - id of manager info
   * @return - true if removed.
   */
  boolean removeManagerInfo(ManagerId id);

  /**
   * update queued time epoch for given taskId into runId.
   *
   * @param runId - runId
   * @param taskId - taskId
   * @return - true if updated, false otherwise
   */
  boolean updateQueuedTime(RunId runId, TaskId taskId);

  /**
   * update start time of given runId, invoked when first task is queued.
   *
   * @param runId - runId
   * @return - true if updated, false otherwise
   */
  boolean updateStartTime(RunId runId);

  /**
   * update start time of given taskId in runId.
   *
   * @param runId -runId
   * @param taskId - taskId
   * @return - true if updated, false otherwise.
   */
  boolean updateStartTime(RunId runId, TaskId taskId, ManagerId processedBy);

  /**
   * Marks task completed with given result. also updates completedTimeEpoch.
   *
   * @param executableTask - executanbleTask
   * @param executionResult - executionResult
   * @return true if updated, false otherwise
   */
  boolean completeTask(ExecutableTask executableTask, ExecutionResult executionResult);

  /**
   * Get task info from underlying storage engine.
   *
   * @param runId - runId
   * @param taskId - taskId
   * @return - Optional of {@link TaskInfo}
   */
  Optional<TaskInfo> getTaskInfo(RunId runId, TaskId taskId);

  /**
   * Get run info from underlying storage engine.
   *
   * @param runId - runId
   * @return - Optional of {@link RunInfo}
   */
  Optional<RunInfo> getRunInfo(RunId runId);

  /**
   * Save {@link RunInfo} into storage engine.
   *
   * @param runInfo - runInfo
   */
  void createRunInfo(RunInfo runInfo);

  /**
   * Updates last updated timestamp into runInfo for given runId.
   *
   * @param runId - runId
   * @return true if updated, false otherwise.
   */
  boolean updateRunInfoEpoch(RunId runId);

  /**
   * Save given list of task infos into db.
   *
   * @param runId - runId
   * @param taskInfos - list of taskInfos to save
   */
  void createTaskInfos(RunId runId, List<TaskInfo> taskInfos);

  /**
   * Cleans up everything from db for given runId.
   *
   * @param runId - runId
   * @return - true if cleanedup, false otherwise
   */
  boolean cleanup(RunId runId);

  /**
   * Query and get all runInfos which are not updated since given duration from current timestamp.
   *
   * @param maxDuration - maxDuration
   * @return - list of stuck runInfos.
   */
  List<RunInfo> getStuckRunInfos(Duration maxDuration);

  PollDelayGenerator heartbeatDelayGenerator();

}
