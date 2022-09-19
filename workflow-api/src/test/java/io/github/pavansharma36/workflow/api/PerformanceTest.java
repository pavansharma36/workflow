package io.github.pavansharma36.workflow.api;

import io.github.pavansharma36.workflow.api.bean.RunEvent;
import io.github.pavansharma36.workflow.api.bean.TaskEvent;
import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import io.github.pavansharma36.workflow.api.bean.task.Task;
import io.github.pavansharma36.workflow.api.bean.task.TaskType;
import io.github.pavansharma36.workflow.api.util.RoundRobinIterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomUtils;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;

@Slf4j
public abstract class PerformanceTest extends BaseTest {

  @Test
  public void testPerformanceSingleClient() throws InterruptedException {
    int numWorkflow = 100;
    int numTaskPerWorkflow = 6;
    TestTaskExecutor taskExecutor = new TestTaskExecutor(numWorkflow * numTaskPerWorkflow);
    WorkflowManager workflowManager = builder()
        .addingTaskExecutor(new TaskType(1, "test"), 100, taskExecutor)
        .build();
    try {
      workflowManager.start();

      long startTime = System.currentTimeMillis();
      log.warn("Submitting {} runs with {} tasks each", numWorkflow, numTaskPerWorkflow);
      for (int i = 0; i < numWorkflow; i++) {
        Task task = loadTestResource("tasks.json");
        workflowManager.submit(task);
      }

      taskExecutor.getLatch().await();
      long totalMillis = System.currentTimeMillis() - startTime;
      log.warn("Completed {} runs in {} millis", numWorkflow, totalMillis);

      taskExecutor.getChecker().assertNoDuplicates();
    } finally {
      closeWorkflow(workflowManager);
    }
  }


  @Test
  public void testPerformanceMultiClient() throws InterruptedException {
    int numWorkflow = 100;
    int numWorkflowManager = 6;
    int numTaskPerWorkflow = 6;
    TestTaskExecutor taskExecutor = new TestTaskExecutor(numWorkflow * numTaskPerWorkflow);

    List<WorkflowManager> workflowManagers = new LinkedList<>();
    for (int i = 0; i < numWorkflowManager; i++) {
      workflowManagers.add(builder()
          .addingTaskExecutor(new TaskType(1, "test"), 100, taskExecutor)
          .build());
    }
    try {
      workflowManagers.forEach(WorkflowManager::start);

      long startTime = System.currentTimeMillis();
      RoundRobinIterator<WorkflowManager> wmi = new RoundRobinIterator<>(workflowManagers);
      log.warn("Submitting {} runs with {} tasks each", numWorkflow, numTaskPerWorkflow);
      for (int i = 0; i < numWorkflow; i++) {
        Task task = loadTestResource("tasks.json");
        wmi.next().submit(task);
      }

      taskExecutor.getLatch().await();
      long totalMillis = System.currentTimeMillis() - startTime;
      log.warn("Completed {} runs in {} millis", numWorkflow, totalMillis);

      taskExecutor.getChecker().assertNoDuplicates();
    } finally {
      workflowManagers.forEach(this::closeWorkflow);
    }
  }

  @Test
  public void testPerformanceMultiClientEvents() throws InterruptedException {
    int numWorkflow = 100;
    int numWorkflowManager = 6;
    int numTaskPerWorkflow = 6;
    TestTaskExecutor taskExecutor = new TestTaskExecutor(numWorkflow * numTaskPerWorkflow);

    List<WorkflowManager> workflowManagers = new LinkedList<>();
    for (int i = 0; i < numWorkflowManager; i++) {
      workflowManagers.add(builder()
          .addingTaskExecutor(new TaskType(1, "test"), 100, taskExecutor)
          .build());
    }

    List<RunId> completedRuns = new LinkedList<>();
    List<RunId> abortedRuns = new LinkedList<>();
    List<Pair<RunId, TaskId>> completedTasks = new LinkedList<>();
    CountDownLatch cl = new CountDownLatch(numWorkflow);
    workflowManagers.forEach(workflowManager -> workflowManager.workflowManagerListener().addListener(
        new WorkflowListener() {
          @Override
          public void onRunEvent(RunEvent event) {
            if (event.getType() == RunEventType.RUN_COMPLETED) {
              completedRuns.add(event.getRunId());
              cl.countDown();
            } else if (event.getType() == RunEventType.RUN_ABORTED) {
              abortedRuns.add(event.getRunId());
              cl.countDown();
            }
          }

          @Override
          public void onTaskEvent(TaskEvent event) {
            if (event.getType() == TaskEventType.TASK_COMPLETED) {
              completedTasks.add(Pair.of(event.getRunId(), event.getTaskId()));
            }
          }
        }));
    try {
      workflowManagers.forEach(WorkflowManager::start);

      long startTime = System.currentTimeMillis();
      RoundRobinIterator<WorkflowManager> wmi = new RoundRobinIterator<>(workflowManagers);
      log.warn("Submitting {} runs with {} tasks each", numWorkflow, numTaskPerWorkflow);

      List<RunId> runToCancel = new LinkedList<>();
      for (int i = 0; i < numWorkflow; i++) {
        Task task = loadTestResource("tasks.json");
        RunId runId = wmi.next().submit(task);
        if (RandomUtils.nextBoolean()) {
          runToCancel.add(runId);
        }
      }

      runToCancel.forEach(r -> wmi.next().cancelRun(r));

      cl.await();

      long totalMillis = System.currentTimeMillis() - startTime;
      log.warn("Completed {} runs in {} millis", numWorkflow, totalMillis);

      Assert.assertTrue(completedRuns.size() >= numWorkflow - runToCancel.size());
      Assert.assertTrue(abortedRuns.size() <= runToCancel.size());
      Assert.assertTrue(completedTasks.size() <= numWorkflow * numTaskPerWorkflow);

      taskExecutor.getChecker().assertNoDuplicates();
    } finally {
      workflowManagers.forEach(this::closeWorkflow);
    }
  }

}
