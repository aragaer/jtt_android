package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4

import static java.util.concurrent.TimeUnit.MINUTES;
import java.util.HashSet;
import java.util.Set;

import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.location.LocationClient;
import com.aragaer.jtt.location.LocationService;


public class DayIntervalService implements LocationClient {
    private final DayIntervalCalculator calculator;
    private Set<DayIntervalClient> clients;
    private DayInterval previousInterval, currentInterval, nextInterval;
    private long currentTime;

    public DayIntervalService(DayIntervalCalculator calculator) {
        this.calculator = calculator;
        clients = new HashSet<DayIntervalClient>();
        currentTime = System.currentTimeMillis();
        currentInterval = DayInterval.Night(0, 0);
    }

    public void onLocationChanged(Location newLocation) {
        calculator.setLocation(newLocation);
        changeInterval();
    }

    public void registerClient(DayIntervalClient newClient) {
        clients.add(newClient);
    }

    public void timeChanged(long timestamp) {
        currentTime = timestamp;
        if (!currentInterval.contains(timestamp))
            changeInterval();
    }

    public DayInterval getCurrentInterval() {
        return currentInterval;
    }

    public DayInterval getPreviousInterval() {
        return previousInterval;
    }

    public DayInterval getNextInterval() {
        return nextInterval;
    }

    private void onIntervalChanged() {
        timeChanged(System.currentTimeMillis());
    }

    private void changeInterval() {
        currentInterval = calculator.getIntervalFor(currentTime);
        previousInterval = calculator.getIntervalFor(currentInterval.getStart() - MINUTES.toMillis(1));
        nextInterval = calculator.getIntervalFor(currentInterval.getEnd() + MINUTES.toMillis(1));
        for (DayIntervalClient client : clients)
            client.intervalChanged(currentInterval);
    }
}
