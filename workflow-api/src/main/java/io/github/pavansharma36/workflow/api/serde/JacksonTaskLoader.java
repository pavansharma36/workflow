package io.github.pavansharma36.workflow.api.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.pavansharma36.workflow.api.bean.id.TaskId;
import io.github.pavansharma36.workflow.api.bean.task.Task;
import io.github.pavansharma36.workflow.api.bean.task.TaskImplType;
import io.github.pavansharma36.workflow.api.bean.task.TaskType;
import io.github.pavansharma36.workflow.api.bean.task.impl.AsyncTask;
import io.github.pavansharma36.workflow.api.bean.task.impl.DecisionTask;
import io.github.pavansharma36.workflow.api.bean.task.impl.IdempotentTask;
import io.github.pavansharma36.workflow.api.bean.task.impl.RootTask;
import io.github.pavansharma36.workflow.api.bean.task.impl.SimpleTask;
import io.github.pavansharma36.workflow.api.util.WorkflowException;

/**
 * load root task from string/reader using {@link com.fasterxml.jackson.databind.ObjectMapper}.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JacksonTaskLoader {

  private static final JacksonSerde JACKSON_SERDE = JacksonSerde.getInstance();

  /**
   * load task from string.
   *
   * @param data - json with task details.
   * @return - root task.
   */
  public static Task loadTask(String data) {
    try {
      return loadTask(JACKSON_SERDE.getMapper().readTree(data));
    } catch (JsonProcessingException e) {
      throw new WorkflowException(e.getMessage(), e);
    }
  }

  /**
   * load task from reader.
   *
   * @param reader - reader of json with task details.
   * @return - root task.
   */
  public static Task loadTask(Reader reader) {
    try {
      return loadTask(JACKSON_SERDE.getMapper().readTree(reader));
    } catch (IOException e) {
      throw new WorkflowException(e.getMessage(), e);
    }
  }

  private static Task loadTask(JsonNode node) {
    Map<TaskId, WorkTask> workMap = new HashMap<>();
    node.get("tasks").forEach(n -> {
      WorkTask workTask = new WorkTask();
      JsonNode taskTypeNode = n.get("taskType");
      workTask.taskId = new TaskId(n.get("taskId").asText());
      workTask.taskType = ((taskTypeNode != null) && !taskTypeNode.isNull())
          ? getTaskType(taskTypeNode) : null;
      workTask.metaData = getMap(n.get("metaData"));
      workTask.implType = TaskImplType.valueOf(n.get("implType").asText());
      workTask.childrenTaskIds = new LinkedList<>();
      workTask.retryCount = n.has("retryCount")
          ? n.get("retryCount").asInt(0) : 0;
      n.get("childrenTaskIds").forEach(c -> workTask.childrenTaskIds.add(c.asText()));

      workMap.put(workTask.taskId, workTask);
    });
    String rootTaskId = node.get("rootTaskId").asText();
    return buildTask(workMap, new HashMap<>(), rootTaskId);
  }

  private static Task buildTask(Map<TaskId, WorkTask> workMap, Map<TaskId, Task> buildMap,
                                String taskIdStr) {
    TaskId taskId = new TaskId(taskIdStr);
    Task builtTask = buildMap.get(taskId);
    if (builtTask != null) {
      return builtTask;
    }

    WorkTask task = workMap.get(taskId);
    if (task == null) {
      String message = "Incoherent serialized task. Missing task: " + taskId;
      log.error(message);
      throw new WorkflowException(message);
    }

    List<Task> childrenTasks =
        task.childrenTaskIds.stream().map(id -> buildTask(workMap, buildMap, id)).collect(
            Collectors.toList());
    Task newTask;
    switch (task.implType) {
      case ROOT:
        newTask = new RootTask(childrenTasks);
        break;
      case SIMPLE:
        newTask = new SimpleTask(taskId, task.taskType, childrenTasks, task.metaData);
        break;
      case IDEMPOTENT:
        newTask = new IdempotentTask(taskId, task.taskType, childrenTasks, task.metaData,
            task.retryCount);
        break;
      case DECISION:
        newTask = new DecisionTask(taskId, task.taskType, childrenTasks, task.metaData);
        break;
      case ASYNC:
        newTask = new AsyncTask(taskId, task.taskType, childrenTasks, task.metaData);
        break;
      default:
        throw new WorkflowException("Unhandled task type");
    }
    buildMap.put(taskId, newTask);
    return newTask;
  }

  private static TaskType getTaskType(JsonNode node) {
    return new TaskType(node.get("version").asInt(), node.get("type").asText());
  }

  private static Map<String, Object> getMap(JsonNode node) {
    Map<String, Object> map = new HashMap<>();
    if (node != null && !node.isNull()) {
      Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> nodeEntry = fields.next();
        map.put(nodeEntry.getKey(), nodeEntry.getValue().asText());
      }
    }
    return map;
  }

  private static class WorkTask {
    TaskId taskId;
    TaskType taskType;
    Map<String, Object> metaData;
    List<String> childrenTaskIds;
    TaskImplType implType;
    int retryCount;
  }

}
