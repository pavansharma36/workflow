package io.github.pavansharma36.workflow.api.util;

import java.util.Iterator;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * round robin iterable for continuous iteration of items in list.
 *
 * @param <T> - element type.
 */
@RequiredArgsConstructor
public class RoundRobinIterator<T> implements Iterator<T> {

  @NonNull
  private final List<T> coll;

  private int index = 0;

  @Override
  public boolean hasNext() {
    return !coll.isEmpty();
  }

  @Override
  public T next() { // NOSONAR
    if (index >= coll.size()) {
      index = 0;
    }
    return coll.get(index++);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
