package io.github.pavansharma36.workflow.api;

import io.github.pavansharma36.workflow.api.executor.ExecutableTask;
import io.github.pavansharma36.workflow.api.executor.ExecutionResult;
import io.github.pavansharma36.workflow.api.executor.TaskExecutionStatus;
import io.github.pavansharma36.workflow.api.executor.TaskExecutor;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestTaskExecutor implements TaskExecutor {
  private final ConcurrentTaskChecker checker = new ConcurrentTaskChecker();
  private final int latchQty;
  private volatile CountDownLatch latch;

  public TestTaskExecutor() {
    this(1);
  }

  public TestTaskExecutor(int latchQty) {
    this.latchQty = latchQty;
    latch = new CountDownLatch(latchQty);
  }

  public CountDownLatch getLatch() {
    return latch;
  }

  public ConcurrentTaskChecker getChecker() {
    return checker;
  }

  public void reset() {
    checker.reset();
    latch = new CountDownLatch(latchQty);
  }

  @Override
  public ExecutionResult execute(WorkflowManager manager, ExecutableTask task) {
    try {
      log.info("Executing {}, type: {}", task.getTaskId(), task.getTaskType());
      checker.add(task.getRunId(), task.getTaskId());
      doRun(task);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } finally {
      checker.decrement();
      latch.countDown();
    }
    return ExecutionResult.builder().status(TaskExecutionStatus.SUCCESS)
        .message("hey")
        .resultMeta(new HashMap<>()).build();
  }


  @SuppressWarnings("UnusedParameters")
  protected void doRun(ExecutableTask task) throws InterruptedException {
    Thread.sleep(1000);
  }
}

