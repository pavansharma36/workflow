package org.one.workflow.api.adapter;

import java.util.Optional;
import org.one.workflow.api.WorkflowManagerLifecycle;
import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.util.PollDelayGenerator;

public interface QueueAdapter extends WorkflowManagerLifecycle {

  PollDelayGenerator pollDelayGenerator();

  void pushTask(ExecutableTask task);

  Optional<ExecutableTask> pollTask(TaskType taskType);

  void pushUpdatedRun(RunId runId);

  Optional<RunId> pollUpdatedRun();

}
