package org.one.workflow.api;

import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.bean.RunEvent;
import org.one.workflow.api.bean.TaskEvent;
import org.one.workflow.api.bean.id.RunId;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.bean.task.impl.SimpleTask;
import org.one.workflow.api.impl.WorkflowManagerBuilder;

public abstract class WorkflowListenerTest extends BaseTest {

  @Test
  public void testBasic() throws Exception {
    TestTaskExecutor taskExecutor = new TestTaskExecutor(2);
    TaskType taskType = new TaskType(1, "test");
    WorkflowManager workflowManager = builder()
        .addingTaskExecutor(taskType, 10, taskExecutor)
        .build();
    try {
      Task task = new SimpleTask(taskType);

      Set<Object> events = new HashSet<>();
      workflowManager.workflowManagerListener().addListener(new WorkflowListener() {
        @Override
        public void onRunEvent(RunEvent event) {
          events.add(event);

          if (event.getType() == RunEventType.RUN_COMPLETED) {
            taskExecutor.getLatch().countDown();
          }
        }

        @Override
        public void onTaskEvent(TaskEvent event) {
          events.add(event);
        }
      });
      workflowManager.start();

      RunId runId = workflowManager.submit(task);

      taskExecutor.getLatch().await();

      Assert.assertTrue(events.contains(new RunEvent(runId,
          WorkflowListener.RunEventType.RUN_STARTED)));

      Assert.assertTrue(events.contains(new TaskEvent(runId, task.getId(),
          WorkflowListener.TaskEventType.TASK_STARTED)));

      Assert.assertEquals(4, events.size());
    } finally {
      closeWorkflow(workflowManager);
    }
  }

}
