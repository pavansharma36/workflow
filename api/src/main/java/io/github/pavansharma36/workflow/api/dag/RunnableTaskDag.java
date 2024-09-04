package io.github.pavansharma36.workflow.api.dag;

import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * POJO to hold details of dag tasks and path.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RunnableTaskDag {

  private TaskId taskId;
  private Collection<TaskId> dependencies;
  private Collection<TaskId> childrens;

}
