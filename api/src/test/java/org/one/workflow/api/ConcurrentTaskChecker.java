package org.one.workflow.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.one.workflow.api.bean.id.TaskId;

class ConcurrentTaskChecker {
  private final Set<TaskId> currentSet = new HashSet<>();
  private final List<TaskId> all = new ArrayList<>();
  private int count = 0;
  private final List<Set<TaskId>> sets = new ArrayList<>();

  synchronized void reset() {
    currentSet.clear();
    all.clear();
    sets.clear();
    count = 0;
  }

  synchronized void add(TaskId taskId) {
    all.add(taskId);
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

  synchronized List<TaskId> getAll() {
    return new ArrayList<>(all);
  }

  synchronized void assertNoDuplicates() {
    Assert.assertEquals(all.size(), new HashSet<>(all).size());   // no dups
  }
}
