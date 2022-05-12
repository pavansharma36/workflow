package org.one.workflow.api.dag;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.one.workflow.api.bean.id.TaskId;

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
