package org.one.workflow.api.queue;

import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.Task;

public interface QueueProducer {
	
	void push(RunId runId, Task root);

}
