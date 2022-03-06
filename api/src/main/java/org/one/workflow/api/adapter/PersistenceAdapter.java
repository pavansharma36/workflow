package org.one.workflow.api.adapter;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.one.workflow.api.WorkflowManagerLifecycle;
import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;

public interface PersistenceAdapter extends WorkflowManagerLifecycle {

	boolean updateQueuedTime(RunId runId, TaskId taskId);

	boolean updateStartTime(RunId runId, TaskId taskId);

	int completeTask(ExecutableTask executableTask, ExecutionResult executionResult);

	Optional<TaskInfo> getTaskInfo(RunId runId, TaskId taskId);

	Optional<RunInfo> getRunInfo(RunId runId);

	void createRunInfo(RunInfo runInfo);

	boolean updateStartTime(RunId runId);

	boolean updateRunInfoEpoch(RunId runId);

	void createTaskInfos(RunId runId, List<TaskInfo> taskInfos);

	boolean cleanup(RunId runId);

	List<RunInfo> getStuckRunInfos(Duration maxDuration);

}
