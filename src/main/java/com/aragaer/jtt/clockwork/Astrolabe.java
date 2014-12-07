package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.location.LocationProvider;


public class Astrolabe {
    private final DayIntervalCalculator calculator;
    private final LocationProvider locationProvider;
    private final long granularity;

    public Astrolabe(DayIntervalCalculator calculator, LocationProvider locationProvider, long granularity) {
        this.calculator = calculator;
        this.locationProvider = locationProvider;
        this.granularity = granularity;
    }

    public DayInterval getCurrentInterval() {
        DayInterval interim = calculator.getIntervalFor(System.currentTimeMillis());
        return interim.modified(round(interim.getStart()), round(interim.getEnd()));
    }

    private long round(long value) {
        value += granularity / 2;
        return value - value % granularity;
    }

    public void updateLocation() {
        calculator.setLocation(locationProvider.getCurrentLocation());
    }
}
