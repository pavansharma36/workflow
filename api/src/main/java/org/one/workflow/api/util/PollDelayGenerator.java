package org.one.workflow.api.util;

import java.time.Duration;

/**
 * api to get duration between polls.
 */
public interface PollDelayGenerator {

  /**
   * get delay before polling again.
   *
   * @param result - wether prev poll had result
   * @return - duration to wait before polling again.
   */
  Duration delay(boolean result);

}
