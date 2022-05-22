package org.one.workflow.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.bean.RunEvent;
import org.one.workflow.api.bean.TaskEvent;
import org.one.workflow.api.bean.id.RunId;
import org.one.workflow.api.bean.id.TaskId;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.bean.task.impl.SimpleTask;
import org.one.workflow.api.executor.ExecutableTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.executor.TaskExecutionStatus;
import org.one.workflow.api.executor.TaskExecutor;
import org.one.workflow.api.impl.WorkflowManagerBuilder;
import org.one.workflow.api.model.RunInfo;


public abstract class NormalTest extends BaseTest {

  private WorkflowManagerBuilder builder() {
    return WorkflowManagerBuilder.builder()
        .withAdapter(adapter());
  }

  @Test
  public void testFailedStop() throws Exception {
    TestTaskExecutor taskExecutor = new TestTaskExecutor(2) {
      @Override
      public ExecutionResult execute(WorkflowManager manager, ExecutableTask task) {
        if (task.getTaskId().getId().equals("task3")) {
          return ExecutionResult.builder().status(TaskExecutionStatus.FAILED_STOP).build();
        }
        return super.execute(manager, task);
      }
    };
    TaskType taskType = new TaskType(1, "test");
    WorkflowAdapter adapter = adapter();
    WorkflowManager workflowManager = builder()
        .addingTaskExecutor(taskType, 10, taskExecutor)
        .build();

    CountDownLatch failedLatch = new CountDownLatch(1);
    workflowManager.workflowManagerListener().addListener(new WorkflowListener() {
      @Override
      public void onRunEvent(RunEvent event) {
        if (event.getType() == RunEventType.RUN_FAILED) {
          failedLatch.countDown();
        }
      }

      @Override
      public void onTaskEvent(TaskEvent event) {
        // nothing here.
      }
    });
    try {
      workflowManager.start();

      Task task4 = new SimpleTask(new TaskId("task4"), taskType, Collections.emptyList());
      Task task3 = new SimpleTask(new TaskId("task3"), taskType, Collections.singletonList(task4));
      Task task2 = new SimpleTask(new TaskId("task2"), taskType, Collections.singletonList(task3));
      Task task1 = new SimpleTask(new TaskId("task1"), taskType, Collections.singletonList(task2));
      RunId runId = workflowManager.submit(task1);
      Assert.assertTrue(taskExecutor.getLatch().await(10, TimeUnit.SECONDS));
      Assert.assertTrue(failedLatch.await(3L, TimeUnit.SECONDS));
      TimeUnit.SECONDS.sleep(1L); // withing one second it should cleanup run.

      Optional<RunInfo> runInfo = adapter.persistenceAdapter().getRunInfo(runId);
      Assert.assertFalse(runInfo.isPresent());

      List<Set<TaskId>> sets = taskExecutor.getChecker().getSets();
      List<Set<TaskId>> expectedSets = Arrays.asList(
          Collections.singleton(new TaskId("task1")),
          Collections.singleton(new TaskId("task2"))
      );
      Assert.assertEquals(expectedSets, sets);
    } finally {
      closeWorkflow(workflowManager);
    }
  }

  @Test
  public void testCanceling() throws Exception {
    Semaphore executionLatch = new Semaphore(0);
    CountDownLatch continueLatch = new CountDownLatch(1);

    TaskExecutor taskExecutor = (w, t) -> {
      executionLatch.release();
      try {
        continueLatch.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      return ExecutionResult.builder().status(TaskExecutionStatus.SUCCESS).build();
    };
    TaskType taskType = new TaskType(1, "test");
    WorkflowManager workflowManager = builder()
        .addingTaskExecutor(taskType, 10, taskExecutor)
        .build();
    try {
      workflowManager.start();

      Task task2 = new SimpleTask(new TaskId(), taskType, Collections.emptyList());
      Task task1 = new SimpleTask(new TaskId(), taskType, Collections.singletonList(task2));

      RunId runId = workflowManager.submit(task1);

      executionLatch.acquire();

      workflowManager.cancelRun(runId);
      continueLatch.countDown();

      Assert.assertFalse(
          executionLatch.tryAcquire(1, 5, TimeUnit.SECONDS));  // no more executions should occur
    } finally {
      closeWorkflow(workflowManager);
    }
  }

  @Test
  public void testSingleClientSimple() throws Exception {
    TestTaskExecutor taskExecutor = new TestTaskExecutor(6);
    WorkflowManager workflowManager = builder()
        .addingTaskExecutor(new TaskType(1, "test"), 10, taskExecutor)
        .build();
    try {
      workflowManager.start();

      Task task = loadTestResource("tasks.json");
      workflowManager.submit(task);

      taskExecutor.getLatch().await();

      List<Set<TaskId>> sets = taskExecutor.getChecker().getSets();
      List<Set<TaskId>> expectedSets = Arrays.asList(
          new HashSet<>(Arrays.asList(new TaskId("task1"), new TaskId("task2"))),
          new HashSet<>(Arrays.asList(new TaskId("task3"), new TaskId("task4"),
              new TaskId("task5"))),
          Collections.singleton(new TaskId("task6"))
      );
      Assert.assertEquals(expectedSets, sets);

      taskExecutor.getChecker().assertNoDuplicates();
    } finally {
      closeWorkflow(workflowManager);
    }
  }

  @Test
  public void testMultiClientSimple() throws Exception {
    final int qty = 4;
    TestTaskExecutor taskExecutor = new TestTaskExecutor(6);
    TaskType taskType = new TaskType(1, "test");
    List<WorkflowManager> workflowManagers = new ArrayList<>();
    for (int i = 0; i < qty; ++i) {
      WorkflowManager workflowManager = builder()
          .addingTaskExecutor(taskType, 10, taskExecutor)
          .build();
      workflowManagers.add(workflowManager);
    }
    try {
      workflowManagers.forEach(WorkflowManager::start);

      Task task = loadTestResource("tasks.json");
      workflowManagers.get(qty - 1).submit(task);

      taskExecutor.getLatch().await();

      List<Set<TaskId>> sets = taskExecutor.getChecker().getSets();
      List<Set<TaskId>> expectedSets = Arrays.asList(
          new HashSet<>(Arrays.asList(new TaskId("task1"), new TaskId("task2"))),
          new HashSet<>(Arrays.asList(new TaskId("task3"), new TaskId("task4"),
              new TaskId("task5"))),
          Collections.singleton(new TaskId("task6"))
      );
      Assert.assertEquals(sets, expectedSets);

      taskExecutor.getChecker().assertNoDuplicates();
    } finally {
      workflowManagers.forEach(this::closeWorkflow);
    }
  }

  @Test
  public void testNoData() throws Exception {
    WorkflowManager workflowManager = builder()
        .addingTaskExecutor(new TaskType(1, "test"), 10, new TestTaskExecutor(1))
        .build();
    workflowManager.start();

    Thread.sleep(1000L);

    Optional<ExecutionResult> taskData =
        workflowManager.getTaskExecutionResult(new RunId(), new TaskId());
    Assert.assertFalse(taskData.isPresent());
  }

  @Test
  public void testTaskData() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    TaskExecutor taskExecutor = (w, t) -> {
      if (!t.getTaskId().getId().equals("t1")) {
        latch.countDown();
      }
      Map<String, Object> resultData = new HashMap<>();
      resultData.put("one", "1");
      resultData.put("two", "2");
      return ExecutionResult.builder().status(TaskExecutionStatus.SUCCESS)
          .message("").resultMeta(resultData).build();
    };
    TaskType taskType = new TaskType(1, "test");
    WorkflowManager workflowManager = builder()
        .addingTaskExecutor(taskType, 10, taskExecutor)
        .build();
    try {
      workflowManager.start();

      TaskId taskId = new TaskId("t1");
      RunId runId = workflowManager.submit(new SimpleTask(taskId, taskType,
          Collections.singletonList(new SimpleTask(taskType))));

      latch.await();

      Optional<ExecutionResult> taskData =
          workflowManager.getTaskExecutionResult(runId, taskId);
      Assert.assertTrue(taskData.isPresent());
      Map<String, String> expected = new HashMap<>();
      expected.put("one", "1");
      expected.put("two", "2");
      Assert.assertEquals(expected, taskData.get().getResultMeta());
    } finally {
      closeWorkflow(workflowManager);
    }
  }

  @Test
  public void testSubTask() throws Exception {
    TaskType taskType = new TaskType(1, "test");
    Task groupAChild = new SimpleTask(taskType);
    Task groupAParent = new SimpleTask(taskType, Collections.singletonList(groupAChild));

    Task groupBTask = new SimpleTask(taskType);

    BlockingQueue<TaskId> tasks = new LinkedBlockingDeque<>();
    CountDownLatch latch = new CountDownLatch(1);
    TaskExecutor taskExecutor = (workflowManager, task) -> {
      tasks.add(task.getTaskId());
      if (task.getTaskId().equals(groupBTask.getId())) {
        try {
          latch.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException();
        }
      }
      RunId subTaskRunId = task.getTaskId().equals(groupAParent.getId())
          ? workflowManager.submit(groupBTask) : null;
      return ExecutionResult.builder().status(TaskExecutionStatus.SUCCESS)
          .message("test").resultMeta(new HashMap<>()).build();
    };
    WorkflowManager workflowManager = builder()
        .addingTaskExecutor(taskType, 10, taskExecutor)
        .build();
    try {
      workflowManager.start();
      workflowManager.submit(groupAParent);

      TaskId polledTaskId = tasks.poll(1000, TimeUnit.MILLISECONDS);
      Assert.assertEquals(groupAParent.getId(), polledTaskId);
      polledTaskId = tasks.poll(1000, TimeUnit.MILLISECONDS);
      Assert.assertEquals(groupBTask.getId(), polledTaskId);

      latch.countDown();

      polledTaskId = tasks.poll(1000, TimeUnit.MILLISECONDS);
      Assert.assertEquals(groupAChild.getId(), polledTaskId);
    } finally {
      closeWorkflow(workflowManager);
    }
  }

  @Test
  public void testMultiTypesExecution() throws Exception {
    TaskType taskType1 = new TaskType(1, "type1");
    TaskType taskType2 = new TaskType(1, "type2");
    TaskType taskType3 = new TaskType(1, "type3");

    TestTaskExecutor taskExecutor = new TestTaskExecutor(6);
    WorkflowManager workflowManager = builder()
        .addingTaskExecutor(taskType1, 10, taskExecutor)
        .addingTaskExecutor(taskType2, 10, taskExecutor)
        .addingTaskExecutor(taskType3, 10, taskExecutor)
        .build();
    try {
      workflowManager.start();

      Task task = loadTestResource("multi-tasks.json");
      workflowManager.submit(task);

      taskExecutor.getLatch().await();

      List<Set<TaskId>> sets = taskExecutor.getChecker().getSets();
      List<Set<TaskId>> expectedSets = Arrays.asList(
          new HashSet<>(Arrays.asList(new TaskId("task1"), new TaskId("task2"))),
              new HashSet<>(Arrays.asList(new TaskId("task3"), new TaskId("task4"),
                  new TaskId("task5"))),
              Collections.singleton(new TaskId("task6"))
          );
      Assert.assertEquals(expectedSets, sets);

      taskExecutor.getChecker().assertNoDuplicates();
    } finally {
      closeWorkflow(workflowManager);
    }
  }

  @Test
  public void testMultiTypes() throws Exception {
    TaskType taskType1 = new TaskType(1, "type1");
    TaskType taskType2 = new TaskType(1, "type2");
    TaskType taskType3 = new TaskType(1, "type3");

    BlockingQueue<TaskId> queue1 = new LinkedBlockingDeque<>();
    TaskExecutor taskExecutor1 = (manager, task) -> {
      queue1.add(task.getTaskId());
      return ExecutionResult.builder()
          .status(TaskExecutionStatus.SUCCESS).build();
    };

    BlockingQueue<TaskId> queue2 = new LinkedBlockingDeque<>();
    TaskExecutor taskExecutor2 = (manager, task) -> {
      queue2.add(task.getTaskId());
      return ExecutionResult.builder()
          .status(TaskExecutionStatus.SUCCESS).build();
    };

    BlockingQueue<TaskId> queue3 = new LinkedBlockingDeque<>();
    TaskExecutor taskExecutor3 = (manager, task) -> {
      queue3.add(task.getTaskId());
      return ExecutionResult.builder()
          .status(TaskExecutionStatus.SUCCESS).build();
    };

    WorkflowManager workflowManager = builder()
        .addingTaskExecutor(taskType1, 10, taskExecutor1)
        .addingTaskExecutor(taskType2, 10, taskExecutor2)
        .addingTaskExecutor(taskType3, 10, taskExecutor3)
        .build();
    try {
      workflowManager.start();

      Task task = loadTestResource("multi-tasks.json");
      workflowManager.submit(task);

      List<Set<TaskId>> expectedSets = Arrays.asList(
          new HashSet<>(Arrays.asList(queue1.poll(1000L, TimeUnit.SECONDS),
              queue1.poll(1000L, TimeUnit.SECONDS))),
          new HashSet<>(Arrays.asList(queue2.poll(1000L, TimeUnit.SECONDS),
              queue2.poll(1000L, TimeUnit.SECONDS))),
          new HashSet<>(Arrays.asList(queue3.poll(1000L, TimeUnit.SECONDS),
              queue3.poll(1000L, TimeUnit.SECONDS))));


      Assert.assertEquals(expectedSets.get(0), new HashSet<>(
          Arrays.asList(new TaskId("task1"), new TaskId("task2"))));
      Assert.assertEquals(expectedSets.get(1), new HashSet<>(
          Arrays.asList(new TaskId("task3"), new TaskId("task4"))));
      Assert.assertEquals(expectedSets.get(2), new HashSet<>(
          Arrays.asList(new TaskId("task5"), new TaskId("task6"))));

      Thread.sleep(1000L);

      Assert.assertNull(queue1.peek());
      Assert.assertNull(queue2.peek());
      Assert.assertNull(queue3.peek());
    } finally {
      closeWorkflow(workflowManager);
    }
  }

  protected abstract WorkflowAdapter adapter();

}
