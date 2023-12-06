package io.github.pavansharma36.workflow.inmemory.adapter;

import io.github.pavansharma36.workflow.api.WorkflowManager;
import io.github.pavansharma36.workflow.api.adapter.base.BasePersistenceAdapter;
import io.github.pavansharma36.workflow.api.bean.id.ManagerId;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import io.github.pavansharma36.workflow.api.executor.ExecutableTask;
import io.github.pavansharma36.workflow.api.executor.ExecutionResult;
import io.github.pavansharma36.workflow.api.model.ManagerInfo;
import io.github.pavansharma36.workflow.api.model.RunInfo;
import io.github.pavansharma36.workflow.api.model.TaskInfo;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InmemoryPersistenceAdapter extends BasePersistenceAdapter {

  private Map<ManagerId, ManagerInfo> managerInfos = new HashMap<>();
  private Map<RunId, RunInfo> runInfos = new HashMap<>();
  private Map<RunId, Map<TaskId, TaskInfo>> taskInfos = new HashMap<>();

  public InmemoryPersistenceAdapter(String namespace, PollDelayGenerator heartbeatDelayGenerator) {
    super(namespace, heartbeatDelayGenerator);
  }

  @Override
  public void start(WorkflowManager workflowManager) {

  }

  @Override
  public void stop() {

  }

  @Override
  public boolean createOrUpdateManagerInfo(ManagerInfo managerInfo) {
    managerInfos.computeIfAbsent(managerInfo.getManagerId(), managerId -> managerInfo);
    return true;
  }

  @Override
  public List<ManagerInfo> getAllManagerInfos() {
    return new ArrayList<>(managerInfos.values());
  }

  @Override
  public boolean removeManagerInfo(ManagerId id) {
    return managerInfos.remove(id) != null;
  }

  @Override
  public boolean updateQueuedTime(RunId runId, TaskId taskId) {
    taskInfos.computeIfPresent(runId, (i, taskInfoMap) -> {
      taskInfoMap.computeIfPresent(taskId, (j, task) -> {
        task.setQueuedTimeEpoch(System.currentTimeMillis());
        return task;
      });
      return taskInfoMap;
    });
    return true;
  }

  @Override
  public boolean updateStartTime(RunId runId) {
    runInfos.computeIfPresent(runId, (i, run) -> {
      run.setStartTimeEpoch(System.currentTimeMillis());
      return run;
    });
    return true;
  }

  @Override
  public boolean updateStartTime(RunId runId, TaskId taskId, ManagerId processedBy) {
    taskInfos.computeIfPresent(runId, (i, taskInfoMap) -> {
      taskInfoMap.computeIfPresent(taskId, (j, task) -> {
        task.setStartTimeEpoch(System.currentTimeMillis());
        task.setProcessedBy(processedBy);
        return task;
      });
      return taskInfoMap;
    });
    return true;
  }

  @Override
  public boolean completeTask(ExecutableTask executableTask, ExecutionResult executionResult) {
    taskInfos.computeIfPresent(executableTask.getRunId(), (i, tasks) -> {
      tasks.computeIfPresent(executableTask.getTaskId(), (j, task) -> {
        task.setCompletionTimeEpoch(System.currentTimeMillis());
        task.setResult(executionResult);
        return task;
      });
      return tasks;
    });
    return true;
  }

  @Override
  public Optional<TaskInfo> getTaskInfo(RunId runId, TaskId taskId) {
    return Optional.ofNullable(taskInfos.getOrDefault(runId, Collections.emptyMap()).get(taskId));
  }

  @Override
  public Optional<RunInfo> getRunInfo(RunId runId) {
    return Optional.ofNullable(runInfos.get(runId));
  }

  @Override
  public void createRunInfo(RunInfo runInfo) {
    runInfos.put(runInfo.getRunId(), runInfo);
  }

  @Override
  public boolean updateRunInfoEpoch(RunId runId) {
    runInfos.computeIfPresent(runId, (i, run) -> {
      run.setLastUpdateEpoch(System.currentTimeMillis());
      return run;
    });
    return true;
  }

  @Override
  public void createTaskInfos(RunId runId, List<TaskInfo> taskInfos) {
    this.taskInfos.put(runId, taskInfos.stream().collect(Collectors.toMap(TaskInfo::getTaskId, t -> t)));
  }

  @Override
  public boolean cleanup(RunId runId) {
    return taskInfos.remove(runId) != null && runInfos.remove(runId) != null;
  }

  @Override
  public List<RunInfo> getStuckRunInfos(Duration maxDuration) {
    // in memory dont stuck.
    return Collections.emptyList();
  }
}
