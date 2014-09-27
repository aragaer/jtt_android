package com.aragaer.jtt.core;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TestWithTz extends TestWatcher {
	private Timezone tz;

	@Override
	protected void starting(Description description) {
		tz = description.getAnnotation(Timezone.class);
	}

	public TimeZone getTimeZone() {
		int offsetMillis = (int) TimeUnit.MINUTES.toMillis(tz.offsetMinutes());
		return TimeZone.getTimeZone(TimeZone.getAvailableIDs(offsetMillis)[0]);
	}
}