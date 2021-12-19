package org.tryout;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import org.one.workflow.api.WorkflowManager;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskType;
import org.one.workflow.api.core.WorkflowManagerBuilder;
import org.one.workflow.api.executor.ExecutionResult;
import org.one.workflow.api.executor.TaskExecutionStatus;
import org.one.workflow.api.executor.TaskExecutor;
import org.one.workflow.api.util.Utils;
import org.one.workflow.redis.WorkflowRedisAdapter;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Hello world!
 *
 */
@Slf4j
public class App {
	public static void main(String[] args) {
		
		TaskType taskTypeA = new TaskType(1, "a");
		TaskType taskTypeB = new TaskType(1, "b");
		TaskType taskTypeC = new TaskType(1, "c");
		TaskType taskTypeR = new TaskType(1, "root");

		Task taskC = new Task(taskTypeC);
		Task taskA = new Task(taskTypeA, Collections.singletonList(taskC));
		Task taskB = new Task(taskTypeB, Collections.singletonList(taskC));
		
		Task root = new Task(taskTypeR, Arrays.asList(taskA, taskB));
		
		TaskExecutor te = (w, t) -> {
			log.info("Executiong {}", t.getTaskType());
			Utils.sleep(Duration.ofSeconds(10));
			return ExecutionResult.builder()
					.status(TaskExecutionStatus.SUCCESS)
					.build();
		};
		
		JedisPool jedisPool = new JedisPool();
		
		WorkflowManager workflowManager = WorkflowManagerBuilder.builder()
				.withAdapter(WorkflowRedisAdapter.builder()
						.jedisPool(jedisPool)
						.namespace("test")
						.build())
				.addingTaskExecutor(taskTypeA, 2, te)
				.addingTaskExecutor(taskTypeB, 2, te)
				.addingTaskExecutor(taskTypeC, 2, te)
				.addingTaskExecutor(taskTypeR, 2, te)
				.build();
		
		workflowManager.start();
		
		workflowManager.submit(root);
		
		
	}
}
