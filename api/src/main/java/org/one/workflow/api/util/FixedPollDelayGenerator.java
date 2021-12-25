package org.one.workflow.api.util;

import java.time.Duration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FixedPollDelayGenerator implements PollDelayGenerator {

	private final Duration pollDuration;

	@Override
	public Duration delay(final boolean result) {
		return pollDuration;
	}

}
