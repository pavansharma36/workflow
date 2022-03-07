package org.one.workflow.api.dag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.bean.task.TaskId;

public class RunnableTaskDagBuilder {

  private final List<RunnableTaskDag> entries;
  private final Map<TaskId, Task> tasks;

  public RunnableTaskDagBuilder(final Task task) {
    entries = new LinkedList<>();
    tasks = new HashMap<>();
    build(task);
  }

  public List<RunnableTaskDag> getEntries() {
    return entries;
  }

  public Map<TaskId, Task> getTasks() {
    return tasks;
  }

  private void build(final Task task) {
    final DefaultDirectedGraph<TaskId, DefaultEdge> graph =
        new DefaultDirectedGraph<>(DefaultEdge.class);
    worker(graph, task, null, new HashSet<>());

    final CycleDetector<TaskId, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
    if (cycleDetector.detectCycles()) {
      throw new RuntimeException("The Task DAG contains cycles: " + task);
    }

    final TopologicalOrderIterator<TaskId, DefaultEdge> orderIterator =
        new TopologicalOrderIterator<>(graph);
    while (orderIterator.hasNext()) {
      final TaskId taskId = orderIterator.next();
      final Set<DefaultEdge> taskIdEdges = graph.edgesOf(taskId);
      final Set<TaskId> dependencies = taskIdEdges.stream().map(graph::getEdgeSource)
          .filter(edge -> !edge.equals(taskId)).collect(Collectors.toSet());
      final Set<TaskId> childrens = taskIdEdges.stream().map(graph::getEdgeTarget)
          .filter(edge -> !edge.equals(taskId)).collect(Collectors.toSet());
      entries.add(new RunnableTaskDag(taskId, dependencies, childrens));
    }
  }

  private void worker(final DefaultDirectedGraph<TaskId, DefaultEdge> graph, final Task task,
                      final TaskId parentId,
                      final Set<TaskId> usedTasksSet) {
    if (usedTasksSet.add(task.getId())) {
      tasks.put(task.getId(), task);
    }

    graph.addVertex(task.getId());
    if (parentId != null) {
      graph.addEdge(parentId, task.getId());
    }

    task.getChildrens().forEach(child -> worker(graph, child, task.getId(), usedTasksSet));
  }
}
