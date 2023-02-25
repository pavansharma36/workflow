package io.github.pavansharma36.workflow.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.pavansharma36.workflow.api.NormalTest;
import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.adapter.builder.WorkflowAdapterBuilder;
import io.github.pavansharma36.workflow.api.util.FixedPollDelayGenerator;
import io.github.pavansharma36.workflow.inmemory.builder.InmemoryQueueAdapterBuilder;
import io.github.pavansharma36.workflow.inmemory.builder.InmemoryScheduleAdapterBuilder;
import io.github.pavansharma36.workflow.mongodb.adapter.builder.MongoDBPersistanceAdapterBuilder;
import java.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoDBNormalTest extends NormalTest {

//  @Rule
//  public GenericContainer redis = new GenericContainer(DockerImageName.parse("mongo"))
//      .withExposedPorts(27017);


  @Override
  protected WorkflowAdapter adapter() {
    final String namespace = "test";
    MongoClient client = MongoClients.create("mongodb://mongoadmin:secret@localhost:27017/test");
    return new WorkflowAdapterBuilder()
        .withPersistenceAdapterBuilder(MongoDBPersistanceAdapterBuilder.builder("test", client)
            .withNamespace(namespace))
        .withScheduleAdapterBuilder(new InmemoryScheduleAdapterBuilder().withNamespace(namespace))
        .withQueueAdapterBuilder(new InmemoryQueueAdapterBuilder().withNamespace(namespace))
        .withSchedulePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofMillis(100L)))
        .withQueuePollDelayGenerator(new FixedPollDelayGenerator(Duration.ofMillis(100L)))
        .build();
  }
}
