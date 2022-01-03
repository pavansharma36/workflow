package org.one.workflow.api;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.one.workflow.api.bean.RunEvent;
import org.one.workflow.api.bean.TaskEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkflowManagerListener {

	private final List<WorkflowListener> listeners = new LinkedList<>(
			Collections.singleton(new LoggerWorkflowListener()));

	public void addListener(final WorkflowListener listener) {
		this.listeners.add(listener);
	}

	public void publishEvent(final RunEvent event) {
		listeners.forEach(l -> {
			try {
				l.onRunEvent(event);
			} catch (final Exception e) {
				log.error("Unhandled error onRunEvent {}", e.getMessage(), e);
			}
		});
	}

	public void publishEvent(final TaskEvent event) {
		listeners.forEach(l -> {
			try {
				l.onTaskEvent(event);
			} catch (final Exception e) {
				log.error("Unhandled error onTaskEvent {}", e.getMessage(), e);
			}
		});
	}

	@Slf4j
	private static final class LoggerWorkflowListener implements WorkflowListener {

		@Override
		public void onRunEvent(final RunEvent event) {
			log.info("Run event {}", event);
		}

		@Override
		public void onTaskEvent(final TaskEvent event) {
			log.info("Task event {}", event);
		}

	}

}
