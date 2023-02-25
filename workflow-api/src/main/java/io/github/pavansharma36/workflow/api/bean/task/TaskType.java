package io.github.pavansharma36.workflow.api.bean.task;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * Task type to differentiate queue and executor for submitted tasks.
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TaskType {
  private int version = 1;
  private @NonNull String type;
}
