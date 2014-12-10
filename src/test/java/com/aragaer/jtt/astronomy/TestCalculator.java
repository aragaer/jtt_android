package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.location.Location;


public class TestCalculator implements DayIntervalCalculator {
    public Location location;
    public int intervalCalls;
    public long timestamp;
    private DayInterval nextResult = DayInterval.Day(0, 0);

    public void setNextResult(DayInterval nextResult) {
        this.nextResult = nextResult;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public DayInterval getIntervalFor(long timestamp) {
        intervalCalls++;
        this.timestamp = timestamp;
        return nextResult;
    }
}
