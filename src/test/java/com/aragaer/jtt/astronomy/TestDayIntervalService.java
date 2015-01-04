package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.location.Location;


public class TestDayIntervalService extends DayIntervalService {

    private DayInterval nextResult;
    public int updateLocationCalls;
    public int dateTimeChangeCalls;
    public Location currentLocation;

    public TestDayIntervalService(DayIntervalCalculator calculator, DateTimeChangeListener listener) {
        super(calculator, listener);
    }

    @Override
    public DayInterval getCurrentInterval() {
        return nextResult == null ? super.getCurrentInterval() : nextResult;
    }

    public void setNextResult(DayInterval interval) {
        nextResult = interval;
    }

    @Override
    public void onDateTimeChanged() {
        dateTimeChangeCalls++;
        super.onDateTimeChanged();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        super.onLocationChanged(location);
    }
}
