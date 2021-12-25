package org.one.workflow.api.bean.task.impl;

import java.util.List;
import java.util.Map;

import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskId;
import org.one.workflow.api.bean.task.TaskType;

import lombok.NonNull;

public class DecisionTask extends IdempotentTask {

	public DecisionTask(@NonNull final TaskType type, @NonNull final List<Task> childrens) {
		super(type, childrens);
	}

	public DecisionTask(@NonNull final TaskType type, @NonNull final List<Task> childrens,
			final Map<String, Object> taskMeta) {
		super(new TaskId(), type, childrens, taskMeta);
	}

	public DecisionTask(@NonNull final TaskId id, @NonNull final TaskType type, @NonNull final List<Task> childrens,
			final Map<String, Object> taskMeta) {
		super(id, type, childrens, taskMeta);
	}

}
