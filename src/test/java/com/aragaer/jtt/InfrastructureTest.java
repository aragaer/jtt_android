package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalService;
import com.aragaer.jtt.astronomy.TestCalculator;
import com.aragaer.jtt.clockwork.TickCounter;
import com.aragaer.jtt.clockwork.TickProvider;
import com.aragaer.jtt.clockwork.TickService;
import com.aragaer.jtt.clockwork.TickClient;
import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.location.LocationProvider;
import com.aragaer.jtt.location.LocationService;


public class InfrastructureTest {

    @Test public void setAllUp() {
        TestLocationProvider locationProvider = new TestLocationProvider();
        LocationService locationService = new LocationService(locationProvider);


        TestCalculator calculator = new TestCalculator();
        DayIntervalService intervalService = new DayIntervalService(calculator);

        TestMetronome metronome = new TestMetronome();
        TickService tickService = new TickService(metronome);

        TestTickClient client = new TestTickClient();

        locationService.registerClient(intervalService);
        intervalService.registerClient(tickService);
        tickService.registerClient(client);
    }

    private static class TestLocationProvider implements LocationProvider {
        public Location getCurrentLocation() {
            return null;
        }
    }

    private static class TestMetronome implements TickProvider {
        public void attachTo(TickCounter cogs) {
        }

        public void start(long start, long tickLength) {
        }

        public void stop() {
        }
    }

    private static class TestTickClient implements TickClient {
        public void tickChanged(int ticks) {
        }
    }
}
