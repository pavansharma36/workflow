package io.github.pavansharma36.workflow.inmemory.adapter;

import io.github.pavansharma36.workflow.api.WorkflowManager;
import io.github.pavansharma36.workflow.api.adapter.QueueAdapter;
import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.task.TaskType;
import io.github.pavansharma36.workflow.api.executor.ExecutableTask;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InmemoryQueueAdapter implements QueueAdapter {

  private final LinkedList<RunId> updatedRunQueue = new LinkedList<>();
  private final Map<TaskType, LinkedList<ExecutableTask>> queue = new HashMap<>();

  private final PollDelayGenerator pollDelayGenerator;

  @Override
  public void start(WorkflowManager workflowManager) {

  }

  @Override
  public void stop() {

  }

  @Override
  public void maintenance(WorkflowAdapter workflowAdapter) {

  }

  @Override
  public PollDelayGenerator pollDelayGenerator() {
    return pollDelayGenerator;
  }

  @Override
  public void pushTask(ExecutableTask task) {
    queue.computeIfAbsent(task.getTaskType(), i -> new LinkedList<>()).offer(task);
  }

  @Override
  public Optional<ExecutableTask> pollTask(TaskType taskType) {
    return Optional.ofNullable(queue.computeIfAbsent(taskType, i -> new LinkedList<>()).poll());
  }

  @Override
  public boolean commitTaskProcessed(ExecutableTask task) {
    return true;
  }

  @Override
  public void pushUpdatedRun(RunId runId) {
    updatedRunQueue.offer(runId);
  }

  @Override
  public Optional<RunId> pollUpdatedRun() {
    return Optional.ofNullable(updatedRunQueue.poll());
  }

  @Override
  public boolean commitUpdatedRunProcess(RunId runId) {
    return true;
  }
}
