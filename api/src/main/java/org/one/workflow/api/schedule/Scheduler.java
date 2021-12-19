package org.one.workflow.api.schedule;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.one.workflow.api.RunnableTaskDag;
import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.TaskExecutionStatus;
import org.one.workflow.api.model.RunInfo;
import org.one.workflow.api.model.TaskInfo;
import org.one.workflow.api.queue.WorkflowDao;
import org.one.workflow.api.util.Utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public class Scheduler implements Runnable, Closeable {

	private final Duration pollDuration;
	private final ScheduleSelector scheduleSelector;
	private final WorkflowDao workflowDao;

	@Override
	public void close() throws IOException {
		scheduleSelector.stop();
	}

	@Override
	public void run() {
		scheduleSelector.start();
		while (!Thread.interrupted()) {
			if (scheduleSelector.isScheduler()) {
				try {
					handleRun();
				} catch (Exception e) {
					log.error("Unknown error {}", e.getMessage(), e);
				}
			} else {
				log.debug("Not scheduler");
			} 
			Utils.sleep(pollDuration);
		}
	}
	
	private void handleRun() {
		Optional<RunId> oRun = workflowDao.pollUpdatedRun();
		while(oRun.isPresent()) {
			RunId runId = oRun.get();
			log.info("Updating run: " + runId);

	        Optional<RunInfo> runInfo = workflowDao.getRunInfo(runId);
	        if ( runInfo.isPresent() ) {
	        	updateRun(runId, runInfo.get());
	        }
			
			oRun = workflowDao.pollUpdatedRun();
		}
	}
	
	private void updateRun(RunId runId, RunInfo runInfo) {
		if ( runInfo.getCompletionTimeEpoch() > 0L ) {
            log.debug("Run is completed. Ignoring: " + runInfo);
            return;
        }
		
		Map<TaskId, TaskInfo> taskInfoCache = new HashMap<>();
		
		boolean completeRun = false;
		for(RunnableTaskDag t : runInfo.getDag()) {
			Optional<TaskInfo> taskO = workflowDao.getTaskInfo(runId, t.getTaskId());
			if(taskO.isPresent()) {
				TaskInfo ti = taskO.get();
				taskInfoCache.put(new TaskId(ti.getTaskId()), ti);
				completeRun = ti.getStatus() == TaskExecutionStatus.FAILED_STOP;
			} else {
				completeRun = true;
			}
			
			if(completeRun) {
				break;
			}
		}

        if ( completeRun ) {
            log.debug("Run has canceled tasks and will be marked completed: " + runId);
            completeRun(runId);
            return; // one or more tasks has canceled the entire run
        }
        
        Set<TaskId> completedTasks = new HashSet<>();
        
        runInfo.getDag().forEach(d -> {
        	TaskId tid = d.getTaskId();
        	TaskInfo taskInfo = taskInfoCache.get(tid);
        	
        	if (taskInfo.getCompletionTimeEpoch() > 0) {
				completedTasks.add(tid);
			} else if (taskInfo.getQueuedTimeEpoch() <= 0) {
				boolean allDependenciesAreComplete = d.getDependencies().stream()
						.allMatch(t -> taskInfoCache.get(t).getCompletionTimeEpoch() > 0);
				if (allDependenciesAreComplete) {
					queueTask(runId, tid, taskInfo);
				}
			}
        });

		if (completedTasks.equals(runInfo.getDag().stream().map(RunnableTaskDag::getTaskId).collect(Collectors.toSet()))) {
			completeRun(runId);
		}
	}
	
	private void queueTask(RunId runId, TaskId taskId, TaskInfo taskInfo) {
		ExecutableTask executableTask = ExecutableTask.builder()
				.runId(runId)
				.taskId(taskId)
				.taskMeta(taskInfo.getTaskMeta())
				.taskType(new TaskType(taskInfo.getVersion(), taskInfo.getType()))
				.build();
		
		workflowDao.pushTask(executableTask);
		workflowDao.updateQueuedTime(runId, taskId);
	}
	
	private void completeRun(RunId runId) {
		log.info("Completing run {}", runId);
		workflowDao.cleanup(runId);
	}

}
