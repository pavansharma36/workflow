package io.github.pavansharma36.workflow.mongodb.rule;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.WorkflowAdapterBuilder;
import io.github.pavansharma36.workflow.api.junit.WorkflowTestRule;
import io.github.pavansharma36.workflow.api.util.FixedPollDelayGenerator;
import io.github.pavansharma36.workflow.inmemory.builder.InmemoryQueueAdapterBuilder;
import io.github.pavansharma36.workflow.inmemory.builder.InmemoryScheduleAdapterBuilder;
import io.github.pavansharma36.workflow.mongodb.adapter.builder.MongoDbPersistanceAdapterBuilder;
import java.time.Duration;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoDBRule extends WorkflowTestRule {

  public final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

  private MongoClient client = null;

  @Override
  protected void pre() {
    mongoDBContainer.start();
    client = MongoClients.create(mongoDBContainer.getConnectionString());
  }

  @Override
  protected void post() {
    client.close();
    mongoDBContainer.stop();
  }


  @Override
  public WorkflowAdapter adapter() {
    final String namespace = "test";
    return new WorkflowAdapterBuilder()
        .withPersistenceAdapterBuilder(MongoDbPersistanceAdapterBuilder.builder("test", client)
            .withNamespace(namespace))
        .withScheduleAdapterBuilder(new InmemoryScheduleAdapterBuilder().withNamespace(namespace))
        .withQueueAdapterBuilder(new InmemoryQueueAdapterBuilder().withNamespace(namespace))
        .withSchedulePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofMillis(100L)))
        .withQueuePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofMillis(100L)))
        .build();
  }
}
