package org.one.workflow.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
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
import org.one.workflow.api.impl.WorkflowManagerBuilder;
import org.one.workflow.api.model.RunInfo;


public abstract class NormalTest extends BaseTest {

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
    WorkflowManager workflowManager = WorkflowManagerBuilder.builder()
        .withAdapter(adapter)
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

  protected abstract WorkflowAdapter adapter();

}
