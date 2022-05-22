package org.one.workflow.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.one.workflow.api.bean.task.Task;
import org.one.workflow.api.serde.JacksonTaskLoader;
import org.one.workflow.api.util.WorkflowException;

public class BaseTest {

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
      workflowManager.close();
    } catch (IOException e) {
      throw new WorkflowException(e.getMessage(), e);
    }
  }

}
