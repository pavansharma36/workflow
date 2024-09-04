package io.github.pavansharma36.workflow.api.helper;

import io.github.pavansharma36.workflow.api.bean.id.RunId;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;

public class ConcurrentTaskChecker {
  private final Set<TaskId> currentSet = new HashSet<>();
  private final List<Pair<RunId, TaskId>> all = new ArrayList<>();
  private int count = 0;
  private final List<Set<TaskId>> sets = new ArrayList<>();

  public synchronized void reset() {
    currentSet.clear();
    all.clear();
    sets.clear();
    count = 0;
  }

  public synchronized void add(RunId runId, TaskId taskId) {
    all.add(Pair.of(runId, taskId));
    currentSet.add(taskId);
    ++count;
  }

  public synchronized void decrement() {
    if (--count == 0) {
      HashSet<TaskId> copy = new HashSet<>(currentSet);
      currentSet.clear();
      count = 0;
      sets.add(copy);
    }
  }

  public synchronized List<Set<TaskId>> getSets() {
    return new ArrayList<>(sets);
  }

  public synchronized List<Pair<RunId, TaskId>> getAll() {
    return new ArrayList<>(all);
  }

  public synchronized void assertNoDuplicates() {
    Assert.assertEquals(all.size(), new HashSet<>(all).size());   // no dups
  }
}
