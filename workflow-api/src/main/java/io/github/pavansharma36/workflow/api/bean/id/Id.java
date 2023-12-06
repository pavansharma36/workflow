package io.github.pavansharma36.workflow.api.bean.id;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.pavansharma36.workflow.api.serde.IdSerializer;
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
@JsonSerialize(using = IdSerializer.class)
public class Id {
  /**
   * string representation of this id.
   */
  private String id; // NOSONAR
}
