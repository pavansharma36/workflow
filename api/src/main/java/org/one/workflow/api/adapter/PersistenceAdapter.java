package org.one.workflow.api.adapter;

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

	int updateQueuedTime(RunId runId, TaskId taskId);

	int updateStartTime(RunId runId, TaskId taskId);

	int completeTask(ExecutableTask executableTask, ExecutionResult executionResult);

	Optional<TaskInfo> getTaskInfo(RunId runId, TaskId taskId);

	Optional<RunInfo> getRunInfo(RunId runId);

	void createRunInfo(RunInfo runInfo);

	void updateStartTime(RunId runId);

	void createTaskInfos(RunId runId, List<TaskInfo> taskInfos);

	boolean cleanup(RunId runId);

}
