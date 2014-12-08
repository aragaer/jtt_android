package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.location.LocationProvider;
import com.aragaer.jtt.test.*;


public class AstrolabeTest {

    private Astrolabe astrolabe;
    private TestCalculator calculator;
    private TestLocationProvider locationProvider;
    private TestClock clock;

    @Before
    public void setup() {
        locationProvider = new TestLocationProvider(new Location(1, 2));
        calculator = new TestCalculator();
        astrolabe = new Astrolabe(calculator, locationProvider);
        astrolabe.updateLocation();
        clock = new TestClock();
        astrolabe.setClock(clock);
    }

    @Test
    public void shouldReturnCalculatorResult() {
        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);

        assertThat(astrolabe.getCurrentInterval(), equalTo(interval));
    }

    @Test
    public void shouldFeedLocationFromProviderToCalculator() {
        Location location = new Location(3, 4);
        locationProvider.setLocation(location);

        astrolabe.updateLocation();

        assertThat(calculator.location, equalTo(location));
    }

    @Test public void shouldNotifyClockOnDateTimeChange() {
        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);

        long before = System.currentTimeMillis();
        astrolabe.onDateTimeChanged();
        long after = System.currentTimeMillis();

        assertThat(clock.currentInterval, equalTo(interval));
        assertThat(calculator.timestamp, greaterThanOrEqualTo(before));
        assertThat(calculator.timestamp, lessThanOrEqualTo(after));
    }

    @Test public void shouldNotifyClockOnLocationChange() {
        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);

        Location location = new Location(4, 5);
        long before = System.currentTimeMillis();
        astrolabe.onLocationChanged(location);
        long after = System.currentTimeMillis();

        assertThat(calculator.location, equalTo(location));
        assertThat(clock.currentInterval, equalTo(interval));
        assertThat(calculator.timestamp, greaterThanOrEqualTo(before));
        assertThat(calculator.timestamp, lessThanOrEqualTo(after));
    }

    private static class TestLocationProvider implements LocationProvider {
        private Location location;

        public TestLocationProvider(Location location) {
            this.location = location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public Location getCurrentLocation() {
            return location;
        }
    }

    private static class TestCalculator implements DayIntervalCalculator {
        public Location location;
        public int intervalCalls;
        public long timestamp;
        private DayInterval nextResult;

        public void setNextResult(DayInterval nextResult) {
            this.nextResult = nextResult;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public DayInterval getIntervalFor(long timestamp) {
            intervalCalls++;
            this.timestamp = timestamp;
            return nextResult;
        }
    }
}
