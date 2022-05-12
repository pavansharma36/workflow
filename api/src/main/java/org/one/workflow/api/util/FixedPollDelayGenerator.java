package org.one.workflow.api.util;

import java.time.Duration;
import lombok.RequiredArgsConstructor;

/**
 * Poll delay generator which gives fixed delay regardless of result.
 */
@RequiredArgsConstructor
public class FixedPollDelayGenerator implements PollDelayGenerator {

  private final Duration pollDuration;

  @Override
  public Duration delay(final boolean result) {
    return pollDuration;
  }

}
