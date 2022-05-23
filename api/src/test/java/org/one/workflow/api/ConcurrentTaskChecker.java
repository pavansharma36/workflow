package org.one.workflow.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.one.workflow.api.bean.id.RunId;
import org.one.workflow.api.bean.id.TaskId;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;

class ConcurrentTaskChecker {
  private final Set<TaskId> currentSet = new HashSet<>();
  private final List<Pair<RunId, TaskId>> all = new ArrayList<>();
  private int count = 0;
  private final List<Set<TaskId>> sets = new ArrayList<>();

  synchronized void reset() {
    currentSet.clear();
    all.clear();
    sets.clear();
    count = 0;
  }

  synchronized void add(RunId runId, TaskId taskId) {
    all.add(Pair.of(runId, taskId));
    currentSet.add(taskId);
    ++count;
  }

  synchronized void decrement() {
    if (--count == 0) {
      HashSet<TaskId> copy = new HashSet<>(currentSet);
      currentSet.clear();
      count = 0;
      sets.add(copy);
    }
  }

  synchronized List<Set<TaskId>> getSets() {
    return new ArrayList<>(sets);
  }

  synchronized List<Pair<RunId, TaskId>> getAll() {
    return new ArrayList<>(all);
  }

  synchronized void assertNoDuplicates() {
    Assert.assertEquals(all.size(), new HashSet<>(all).size());   // no dups
  }
}
