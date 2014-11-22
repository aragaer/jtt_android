package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.location.LocationProvider;
import com.aragaer.jtt.location.Location;


public class ClockTest {

    private Clock clock;
    private TestMetronome metronome;
    private TestAstrolabe astrolabe;
    private TestCalculator calculator;
    private TestLocationProvider locationProvider;

    @Before
    public void setUp() {
        metronome = new TestMetronome();
        calculator = new TestCalculator();
        locationProvider = new TestLocationProvider();
        astrolabe = new TestAstrolabe(calculator, locationProvider, 1);
        clock = new Clock(astrolabe, metronome);
    }

    @Test
    public void shouldTriggerEvent() {
        TestEvent event = new TestEvent();
        clock.addClockEvent(event);
        metronome.tick(42);
        assertThat(event.lastTriggeredAt, equalTo(42));
    }

    @Test
    public void shouldTriggerEventWithGranularity() {
        TestEvent event = new TestEvent(20);
        clock.addClockEvent(event);
        metronome.tick(42);
        assertThat(event.lastTriggeredAt, equalTo(40));
    }

    @Test
    public void shouldTriggerMultipleEvents() {
        TestEvent event1 = new TestEvent(20);
        TestEvent event2 = new TestEvent(1);
        clock.addClockEvent(event1);
        clock.addClockEvent(event2);
        metronome.tick(42);
        assertThat(event1.lastTriggeredAt, equalTo(40));
        assertThat(event2.lastTriggeredAt, equalTo(42));
    }

    @Test
    public void shouldUpdateLocationWhenAdjusted() {
        Location location = new Location(1, 2);
        locationProvider.setCurrentLocation(location);
        calculator.setNextResult(DayInterval.Day(0, 1));
        clock.adjust();
        assertThat(calculator.location, equalTo(location));
    }

    private static class TestEvent implements ClockEvent {
        int lastTriggeredAt;
        final int granularity;

        public TestEvent() {
            this(1);
        }

        public TestEvent(int granularity) {
            this.granularity = granularity;
        }

        public void trigger(int ticks) {
            lastTriggeredAt = ticks;
        }

        public int getGranularity() {
            return granularity;
        }
    }

    private static class TestMetronome implements Metronome {

        private Clockwork clockwork;

        public void attachTo(Clockwork clockwork) {
            this.clockwork = clockwork;
        }

        public void start(long start, long tickLength) {}

        public void stop() {}

        public void tick(int times) {
            clockwork.tick(times);
        }
    }

    private static class TestCalculator implements DayIntervalCalculator {
        public Location location;
        private DayInterval nextResult;

        public void setNextResult(DayInterval nextResult) {
            this.nextResult = nextResult;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public DayInterval getIntervalFor(long timestamp) {
            return nextResult;
        }
    }

    private static class TestLocationProvider implements LocationProvider {
        private Location location;
        public void setCurrentLocation(Location location) {
            this.location = location;
        }
        public Location getCurrentLocation() {
            return location;
        }
    }

    private static class TestAstrolabe extends Astrolabe {
        public TestAstrolabe(DayIntervalCalculator calculator, LocationProvider locationProvider, long granularity) {
            super(calculator, locationProvider, granularity);
        }
    }

}
