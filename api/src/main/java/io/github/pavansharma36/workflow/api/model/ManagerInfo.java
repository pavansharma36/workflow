package io.github.pavansharma36.workflow.api.model;

import io.github.pavansharma36.workflow.api.bean.id.ManagerId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * when workflow manager is started its details are persisted.
 * managerId will be used to track, task being processed by current workflow manager.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerInfo {

  public static ManagerInfo getInstance() {
    long timestamp = System.currentTimeMillis();
    return new ManagerInfo(new ManagerId(), timestamp, timestamp);
  }

  private ManagerId managerId;
  private long startTimeEpoch;
  private long heartbeatEpoch;
}
