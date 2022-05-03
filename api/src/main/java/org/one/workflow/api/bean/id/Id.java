package org.one.workflow.api.bean.id;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A base class for all id classes.
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Id {
  /**
   * string representation of this id.
   */
  private String id; // NOSONAR
}
