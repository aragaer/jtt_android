package com.aragaer.jtt.test;
// vim: et ts=4 sts=4 sw=4

import java.util.Calendar;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;


public class TestWithTime extends TestWatcher {
    private long timestamp;

    @Override
    protected void starting(Description description) {
        TestTime time = description.getAnnotation(TestTime.class);
        Calendar calendar = Calendar.getInstance(/* FIXME: timezone ? */);
        calendar.set(time.year(), time.month(), time.day(), time.hour(), time.minute(), time.second());
        calendar.set(Calendar.MILLISECOND, 0);
        timestamp = calendar.getTimeInMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
