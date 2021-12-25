package org.one.workflow.api.executor;

import java.util.Map;

import org.one.workflow.api.bean.run.RunId;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.bean.task.TaskType;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class ExecutableTask {

	private final RunId runId;
	private final TaskId taskId;
	private final TaskType taskType;
	private final Map<String, Object> taskMeta;

}
