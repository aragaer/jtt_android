package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.location.LocationClient;
import com.aragaer.jtt.location.LocationService;


public class DayIntervalService implements LocationClient, DayIntervalEndObserver {
    private final DayIntervalCalculator calculator;
    private final DateTimeChangeListener changeNotifier;
    private DayIntervalClient client;

    public DayIntervalService(DayIntervalCalculator calculator, DateTimeChangeListener listener) {
        this.calculator = calculator;
        this.changeNotifier = listener;
        listener.setService(this);
    }

    public DayInterval getCurrentInterval() {
        return calculator.getIntervalFor(System.currentTimeMillis());
    }

    private void onIntervalChanged() {
        if (client != null)
            client.intervalChanged(getCurrentInterval());
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

    public void registerClient(DayIntervalClient newClient) {
        client = newClient;
    }
}
