package org.one.workflow.api.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.dag.RunnableTaskDagBuilder;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.executor.TaskExecutor;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;
import org.one.workflow.api.queue.QueueConsumer;
import org.one.workflow.api.queue.WorkflowDao;
import org.one.workflow.api.schedule.Scheduler;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkflowManagerImpl implements WorkflowManager {
	
	private final WorkflowAdapter adapter;
	private final List<QueueConsumer> consumers;
	private final Scheduler scheduler;
	
	private WorkflowDao workflowDao;
	
	@Override
	public void close() throws IOException {
		adapter.preClose();
		
		consumers.forEach(t -> {
			try {
				t.close();
			} catch (IOException e) {
				log.error("Error while closing consumer {}", e.getMessage(), e);
			}
		});
		
		adapter.postClose();
	}

	@Override
	public void start() {
		adapter.preStart();
		
		workflowDao = adapter.workflowDao();
		
		consumers.forEach(c -> new Thread(c).start());
		
		new Thread(scheduler).start();
		
		adapter.postStart();
	}

	@Override
	public RunId submit(Task root) {
		return submit(new RunId(), root);
	}

	@Override
	public RunId submit(RunId runId, Task root) {
		RunnableTaskDagBuilder builder = new RunnableTaskDagBuilder(root);
        
        RunInfo runInfo = new RunInfo();
        runInfo.setRunId(runId.getId());
        runInfo.setDag(builder.getEntries());
        
        workflowDao.createRunInfo(runInfo);
        
        for(Task task : builder.getTasks().values()) {
        	TaskInfo taskInfo = new TaskInfo(runId, task);
        	
        	workflowDao.createTaskInfo(runId, taskInfo);
        }
        
        // for scheduler to pick.
        workflowDao.pushUpdatedRun(runId);
        
        return runId;
	}

	@Override
	public boolean cancelRun(RunId runId) {
		return false;
	}

	@Override
	public Optional<ExecutionResult> getTaskExecutionResult(RunId runId, TaskId taskId) {
		return null;
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
