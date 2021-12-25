package org.one.workflow.examples;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.adapter.WorkflowAdapter;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.bean.task.impl.RootTask;
import org.one.workflow.api.bean.task.impl.SimpleTask;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.executor.TaskExecutionStatus;
import org.one.workflow.api.executor.TaskExecutor;
import org.one.workflow.api.impl.WorkflowManagerBuilder;
import org.one.workflow.api.util.Utils;
import org.one.workflow.redis.adapter.builder.JedisWorkflowAdapterBuilder;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;

/**
 * Hello world!
 *
 */
@Slf4j
public class App {
	public static void main(final String[] args) {

		final TaskType taskTypeA = new TaskType(1, "a");
		final TaskType taskTypeB = new TaskType(1, "b");
		final TaskType taskTypeC = new TaskType(1, "c");
		final TaskType taskTypeR = new TaskType(1, "root");

		final Task taskC = new SimpleTask(taskTypeC);
		final Task taskA = new SimpleTask(taskTypeA, Collections.singletonList(taskC));
		final Task taskB = new SimpleTask(taskTypeB, Collections.singletonList(taskC));

		final Task root = new RootTask(Arrays.asList(taskA, taskB));

		final TaskExecutor te = (w, t) -> {
			log.info("Executing {}", t.getTaskType());
			Utils.sleep(Duration.ofSeconds(10));
			return ExecutionResult.builder().status(TaskExecutionStatus.SUCCESS).build();
		};

		final JedisPool jedisPool = new JedisPool();
		final String namespace = "test";
		final WorkflowAdapter adapter = JedisWorkflowAdapterBuilder.builder(jedisPool, namespace).build();

		final WorkflowManager workflowManager = WorkflowManagerBuilder.builder().withAdapter(adapter)
				.addingTaskExecutor(taskTypeA, 2, te).addingTaskExecutor(taskTypeB, 2, te)
				.addingTaskExecutor(taskTypeC, 2, te).addingTaskExecutor(taskTypeR, 2, te).build();

		workflowManager.start();

		workflowManager.submit(root);

	}
}
