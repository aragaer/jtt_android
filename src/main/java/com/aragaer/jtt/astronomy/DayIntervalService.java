package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.location.LocationClient;
import com.aragaer.jtt.location.LocationService;


public class DayIntervalService implements LocationClient {
    private final DayIntervalCalculator calculator;
    private DayIntervalClient client;
    private DayInterval currentInterval;
    private long currentTime;

    public DayIntervalService(DayIntervalCalculator calculator) {
        this.calculator = calculator;
        currentTime = System.currentTimeMillis();
        currentInterval = DayInterval.Night(0, 0);
    }

    public void onLocationChanged(Location newLocation) {
        calculator.setLocation(newLocation);
        changeInterval();
    }

    public void registerClient(DayIntervalClient newClient) {
        client = newClient;
    }

    public void timeChanged(long timestamp) {
        currentTime = timestamp;
        if (!currentInterval.contains(timestamp))
            changeInterval();
    }

    private void onIntervalChanged() {
        timeChanged(System.currentTimeMillis());
    }

    private void changeInterval() {
        currentInterval = calculator.getIntervalFor(currentTime);
        if (client != null)
            client.intervalChanged(currentInterval);
    }
}
