package io.github.pavansharma36.workflow.mongodb.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MongoDBQueryHelper {

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ManagerInfo {
    public static final String MANAGER_ID_KEY = "managerId";
    public static final String START_TIME_KEY = "startTimeEpoch";
    public static final String HEARTBEAT_KEY = "heartbeatEpoch";

    public static String collectionName(String namespace) {
      return namespace + "_manager_info";
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class TaskInfo {

    public static final String QUEUED_TIME_KEY = "queuedTimeEpoch";
    public static final String RUN_ID_KEY = "runId";
    public static final String TASK_ID_KEY = "taskId";
    public static final String START_TIME_KEY = "startTimeEpoch";
    public static final String PROCESSED_BY_KEY = "processedBy";
    public static final String COMPLETION_TIME_KEY = "completionTimeEpoch";
    public static final String RESULT_KEY = "result";

    public static String collectionName(String namespace) {
      return namespace + "_task_info";
    }
  }

  public static class RunInfo {

    public static final String RUN_ID_KEY = "runId";
    public static final String QUEUED_TIME_KEY = "queuedTime";
    public static final String START_TIME_KEY = "startTimeEpoch";
    public static final String LAST_UPDATE_KEY = "lastUpdateEpoch";

    public static String collectionName(String namespace) {
      return namespace + "_run_info";
    }
  }



}
