package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.location.LocationConsumer;
import com.aragaer.jtt.location.LocationService;


public class DayIntervalService implements LocationConsumer, DayIntervalEndObserver {
    private final DayIntervalCalculator calculator;
    private LocationService locationProvider;
    private DayIntervalConsumer consumer;

    public DayIntervalService(DayIntervalCalculator calculator) {
        this.calculator = calculator;
    }

    public DayInterval getCurrentInterval() {
        return calculator.getIntervalFor(System.currentTimeMillis());
    }

    public void updateLocation() {
        calculator.setLocation(locationProvider.getCurrentLocation());
    }

    private void onIntervalChanged() {
        consumer.setInterval(getCurrentInterval());
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

    public void bindToClock(DayIntervalConsumer newClock) {
        consumer = newClock;
    }
}
