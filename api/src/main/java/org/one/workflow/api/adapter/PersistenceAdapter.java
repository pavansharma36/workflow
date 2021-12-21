package org.one.workflow.api.adapter;

import java.util.Optional;

import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;

public interface PersistenceAdapter {

	int updateQueuedTime(RunId runId, TaskId taskId);

	int updateStartTime(RunId runId, TaskId taskId);

	int completeTask(RunId runId, TaskId taskId, ExecutableTask executableTask, ExecutionResult executionResult);

	Optional<TaskInfo> getTaskInfo(RunId runId, TaskId taskId);

	Optional<RunInfo> getRunInfo(RunId runId);

	void createRunInfo(RunInfo runInfo);

	void createTaskInfo(RunId runId, TaskInfo taskInfo);

	void cleanup(RunId runId);

}
