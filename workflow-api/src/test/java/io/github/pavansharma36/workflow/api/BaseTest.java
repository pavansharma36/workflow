package io.github.pavansharma36.workflow.api;

import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import io.github.pavansharma36.workflow.api.bean.task.Task;
import io.github.pavansharma36.workflow.api.impl.WorkflowManagerBuilder;
import io.github.pavansharma36.workflow.api.serde.JacksonTaskLoader;
import io.github.pavansharma36.workflow.api.util.WorkflowException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseTest {

  protected WorkflowManagerBuilder builder() {
    log.warn("Building workflow manager");
    return WorkflowManagerBuilder.builder()
        .withAdapter(adapter());
  }

  protected Task loadTestResource(String path) {
    try (Reader reader = new InputStreamReader(
        getClass().getClassLoader().getResourceAsStream(path))) {
      return JacksonTaskLoader.loadTask(reader);
    } catch (IOException e) {
      throw new WorkflowException(e.getMessage(), e);
    }
  }

  protected void closeWorkflow(WorkflowManager workflowManager) {
    try {
      log.warn("Closing workflow manager {}", workflowManager);
      workflowManager.close();
    } catch (IOException e) {
      throw new WorkflowException(e.getMessage(), e);
    }
  }

  protected abstract WorkflowAdapter adapter();

}
