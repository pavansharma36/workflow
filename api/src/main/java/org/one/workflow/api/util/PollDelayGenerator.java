package org.one.workflow.api.util;

import java.time.Duration;

public interface PollDelayGenerator {

  Duration delay(boolean result);

}
