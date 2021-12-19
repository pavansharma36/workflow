package org.one.workflow.api;

import java.io.Closeable;
import java.util.Optional;

import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.executor.ExecutionResult;

public interface WorkflowManager extends Closeable {
	
	void start();

	RunId submit(Task root);
	
	RunId submit(RunId runId, Task root);
	
	boolean cancelRun(RunId runId);
	
	Optional<ExecutionResult> getTaskExecutionResult(RunId runId, TaskId taskId);
	
}
