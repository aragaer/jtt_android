package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.astronomy.DayInterval;


public class TestAstrolabe extends Astrolabe {

    private DayInterval nextResult;
    public int updateLocationCalls;

    public TestAstrolabe() {
        super(null, null, 1);
    }

    @Override
    public DayInterval getCurrentInterval() {
        return nextResult;
    }

    @Override
    public void updateLocation() {
        updateLocationCalls++;
    }

    public void setNextResult(DayInterval interval) {
        nextResult = interval;
    }
}
