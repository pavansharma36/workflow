package org.one.workflow.examples;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.one.workflow.api.WorkflowListener;
import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.bean.RunEvent;
import org.one.workflow.api.bean.TaskEvent;
import org.one.workflow.api.bean.id.TaskId;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.bean.task.impl.DecisionTask;
import org.one.workflow.api.bean.task.impl.RootTask;
import org.one.workflow.api.bean.task.impl.SimpleTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.executor.TaskExecutionStatus;
import org.one.workflow.api.executor.TaskExecutor;
import org.one.workflow.api.impl.WorkflowManagerBuilder;
import org.one.workflow.api.util.FixedPollDelayGenerator;
import org.one.workflow.api.util.Utils;
import org.one.workflow.redis.adapter.builder.JedisWorkflowAdapterBuilder;
import redis.clients.jedis.JedisPool;

/**
 * Hello world!
 */
@Slf4j
public class App {

  private static final int SUBMIT_COUNT = 10;

  /**
   * main.
   *
   * @param args - submit
   * @throws InterruptedException - thrown
   */
  public static void main(final String[] args) throws InterruptedException, IOException {

    boolean submit = args.length > 0 && "submit".equals(args[0]);

    final TaskType decisionType = new TaskType(1, "decision");
    final TaskType taskTypeA = new TaskType(1, "a");
    final TaskType taskTypeB = new TaskType(1, "b");
    final TaskType taskTypeC = new TaskType(1, "c");

    final Task taskF = new SimpleTask(taskTypeB);
    final Task taskD = new SimpleTask(taskTypeA, Collections.singletonList(taskF));

    final Task taskG = new SimpleTask(taskTypeA);
    final Task taskE =
        new SimpleTask(new TaskId("taske"), taskTypeB, Collections.singletonList(taskG), null);

    final Task decision = new DecisionTask(decisionType, Arrays.asList(taskD, taskE));

    final Task taskC = new SimpleTask(taskTypeC, Collections.singletonList(decision));
    final Task taskA = new SimpleTask(taskTypeA, Collections.singletonList(taskC));
    final Task taskB = new SimpleTask(taskTypeB, Collections.singletonList(taskC));

    final TaskExecutor te = (w, t) -> {
      log.info("Executing {}", t.getTaskType());
      Utils.sleep(Duration.ofMillis(10));
      return ExecutionResult.builder().status(TaskExecutionStatus.SUCCESS).build();
    };

    final JedisPool jedisPool = new JedisPool();
    final String namespace = "test";
    final WorkflowAdapter adapter =
        JedisWorkflowAdapterBuilder.builder(jedisPool, namespace)
            .withMaintenancePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofSeconds(30)))
            .withMaxRunDuration(Duration.ofMinutes(15L))
            .build();

    final WorkflowManager workflowManager = WorkflowManagerBuilder.builder().withAdapter(adapter)
        .addingTaskExecutor(taskTypeA, 2, te).addingTaskExecutor(taskTypeB, 2, te)
        .addingTaskExecutor(taskTypeC, 2, te).addingTaskExecutor(decisionType, 1,
            (manager, task) -> ExecutionResult.builder()
                .status(TaskExecutionStatus.SUCCESS).decision(new TaskId("taske"))
                .build()).build();

    CountDownLatch countDownLatch = new CountDownLatch(SUBMIT_COUNT);
    workflowManager.workflowManagerListener().addListener(new WorkflowListener() {
      @Override
      public void onRunEvent(RunEvent event) {
        if (event.getType() == RunEventType.RUN_COMPLETED) {
          countDownLatch.countDown();
        }
      }

      @Override
      public void onTaskEvent(TaskEvent event) {
        // nothing to do.
      }
    });

    workflowManager.start();

    long startTimeMillis = System.currentTimeMillis();
    if (submit) {
      for (int i = 0; i < SUBMIT_COUNT; i++) {
        final Task root = new RootTask(Arrays.asList(taskA, taskB));
        workflowManager.submit(root);
      }
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        workflowManager.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }));

    countDownLatch.await(10L, TimeUnit.MINUTES);

    log.info("Completed {} runs in {} millis", SUBMIT_COUNT,
        System.currentTimeMillis() - startTimeMillis);

    workflowManager.close();
  }
}
