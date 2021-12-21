package org.one.workflow.api.bean.task.impl;

import java.util.List;

import org.one.workflow.api.bean.task.Task;

public class RootTask extends Task {

	public RootTask(final List<Task> childrens) {
		super(null, childrens);
	}

}
