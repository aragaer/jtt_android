package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import javax.inject.Inject;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.location.Location;


public class TestAstrolabe extends Astrolabe {

    private DayInterval nextResult;
    public int updateLocationCalls;
    public int dateTimeChangeCalls;
    public Location currentLocation;

    public TestAstrolabe(Clock clock) {
        this(null, clock);
    }

    @Inject
    public TestAstrolabe(DayIntervalCalculator calculator, Clock clock) {
        super(calculator, clock);
    }

    @Override
    public DayInterval getCurrentInterval() {
        return nextResult == null ? super.getCurrentInterval() : nextResult;
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

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        super.onLocationChanged(location);
    }
}
