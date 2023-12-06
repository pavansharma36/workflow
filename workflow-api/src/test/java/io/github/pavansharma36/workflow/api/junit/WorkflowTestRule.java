package io.github.pavansharma36.workflow.api.junit;

import io.github.pavansharma36.workflow.api.adapter.WorkflowAdapter;
import java.util.LinkedList;
import java.util.List;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

public abstract class WorkflowTestRule implements TestRule {

  public abstract WorkflowAdapter adapter();

  @Override
  public Statement apply(Statement statement, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        List<Throwable> errors = new LinkedList<>();
        try {
          pre();
          statement.evaluate();
        } catch (Throwable e) {
          errors.add(e);
        } finally {
          post();
        }
        MultipleFailureException.assertEmpty(errors);
      }
    };
  }

  protected abstract void pre();

  protected abstract void post();
}
