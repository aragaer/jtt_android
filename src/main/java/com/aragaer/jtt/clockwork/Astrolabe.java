package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import javax.inject.Inject;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.location.LocationProvider;


public class Astrolabe {
    private final DayIntervalCalculator calculator;
    private LocationProvider locationProvider;
    private Clock clock;

    public Astrolabe(DayIntervalCalculator calculator, LocationProvider locationProvider) {
        this.calculator = calculator;
        this.locationProvider = locationProvider;
    }

    @Inject
    public Astrolabe(DayIntervalCalculator calculator, Clock clock) {
        this.calculator = calculator;
        this.clock = clock;
    }

    public DayInterval getCurrentInterval() {
        return calculator.getIntervalFor(System.currentTimeMillis());
    }

    public void updateLocation() {
        calculator.setLocation(locationProvider.getCurrentLocation());
    }

    private void onIntervalChanged() {
        clock.setInterval(getCurrentInterval());
    }

    public void onDateTimeChanged() {
        onIntervalChanged();
    }

    public void onIntervalEnded() {
        onIntervalChanged();
    }

    public void onLocationChanged(Location newLocation) {
        calculator.setLocation(newLocation);
        onIntervalChanged();
    }

    public void setClock(Clock newClock) {
        clock = newClock;
    }
}