package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.astronomy.DayInterval;


public class TestAstrolabe extends Astrolabe {

    private DayInterval nextResult;
    public int updateLocationCalls;
    public int dateTimeChangeCalls;

    public TestAstrolabe() {
        super(null, null);
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

    @Override
    public void onDateTimeChanged() {
        dateTimeChangeCalls++;
        super.onDateTimeChanged();
    }
}
