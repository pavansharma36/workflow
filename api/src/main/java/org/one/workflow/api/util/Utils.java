package org.one.workflow.api.util;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

  public static String random() {
    return UUID.randomUUID().toString();
  }

  public static void sleep(Duration duration) {
    try {
      Thread.sleep(duration.toMillis());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public static <T> Collection<T> nullSafe(Collection<T> c) {
    return c == null ? Collections.emptyList() : c;
  }

  public static <T> List<T> nullSafe(List<T> c) {
    return c == null ? Collections.emptyList() : c;
  }

  public static <T> Set<T> nullSafe(Set<T> c) {
    return c == null ? Collections.emptySet() : c;
  }

}
