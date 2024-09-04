package io.github.pavansharma36.workflow.api.adapter.base;

import io.github.pavansharma36.workflow.api.adapter.PersistenceAdapter;
import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.model.ManagerInfo;
import io.github.pavansharma36.workflow.api.serde.Deserializer;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.api.serde.Serializer;
import io.github.pavansharma36.workflow.api.util.PollDelayGenerator;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for all {@link PersistenceAdapter}.
 */
@Slf4j
public abstract class BasePersistenceAdapter extends BaseAdapter implements PersistenceAdapter {

  protected BasePersistenceAdapter(String namespace,
                                PollDelayGenerator heartbeatDelayGenerator) {
    super(namespace, heartbeatDelayGenerator);
  }

  @Override
  public void maintenance(WorkflowAdapter adapter) {
    List<ManagerInfo> managerInfos = getAllManagerInfos();
    long minHeartbeatTimestamp = System.currentTimeMillis()
        - (heartbeatDelayGenerator().delay(false).toMillis() * 15);
    managerInfos.forEach(m -> {
      if (m.getHeartbeatEpoch() < minHeartbeatTimestamp) {
        log.info("WorkflowManager's heartbeat is not updated since {}, purging",
            new Date(m.getHeartbeatEpoch()));
        removeManagerInfo(m.getManagerId());
      }
    });
  }

  @Override
  public PollDelayGenerator heartbeatDelayGenerator() {
    return pollDelayGenerator;
  }
}
