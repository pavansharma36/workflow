package org.one.workflow.api.dag;

import java.util.Collection;

import org.one.workflow.api.bean.task.TaskId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RunnableTaskDag {

	private TaskId taskId;
	private Collection<TaskId> dependencies;
	private Collection<TaskId> childrens;

}
