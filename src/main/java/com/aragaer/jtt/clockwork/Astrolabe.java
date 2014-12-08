package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.location.LocationProvider;


public class Astrolabe {
    private final DayIntervalCalculator calculator;
    private final LocationProvider locationProvider;
    private Clock clock;

    public Astrolabe(DayIntervalCalculator calculator, LocationProvider locationProvider) {
        this.calculator = calculator;
        this.locationProvider = locationProvider;
    }

    public DayInterval getCurrentInterval() {
        return calculator.getIntervalFor(System.currentTimeMillis());
    }

    public void updateLocation() {
        calculator.setLocation(locationProvider.getCurrentLocation());
    }

    public void onDateTimeChanged() {
        clock.setInterval(getCurrentInterval());
    }

    public void setClock(Clock newClock) {
        clock = newClock;
    }
}
