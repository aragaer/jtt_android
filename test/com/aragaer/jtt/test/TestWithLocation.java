package com.aragaer.jtt.test;
// vim: et ts=4 sts=4 sw=4

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;


public class TestWithLocation extends TestWatcher {
    private TestLocation location;

    @Override
    protected void starting(Description description) {
        location = description.getAnnotation(TestLocation.class);
    }

    public double getLatitude() {
        return location.latitude();
    }

    public double getLongitude() {
        return location.longitude();
    }
}
