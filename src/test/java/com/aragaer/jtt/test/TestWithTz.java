package com.aragaer.jtt.test;
// vim: et ts=4 sts=4 sw=4

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TestWithTz extends TestWatcher {
    private TestTimezone tz;

    @Override
    protected void starting(Description description) {
        tz = description.getAnnotation(TestTimezone.class);
    }

    public TimeZone getTimeZone() {
        int offsetMillis = (int) TimeUnit.MINUTES.toMillis(tz.offsetMinutes());
        return TimeZone.getTimeZone(TimeZone.getAvailableIDs(offsetMillis)[0]);
    }
}
