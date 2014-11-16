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
    private ManualMetronome metronome;

    @Before
    public void setUp() {
        metronome = new ManualMetronome();
        Astrolabe astrolabe = new Astrolabe(new NullCalculator(), new NullLocationProvider(), 1);
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

    private static class ManualMetronome implements Metronome {

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

    private static class NullCalculator implements DayIntervalCalculator {
        public void setLocation(Location location) {}
        public DayInterval getIntervalFor(long timestamp) {
            return null;
        }
    }

    private static class NullLocationProvider implements LocationProvider {
        public Location getCurrentLocation() {
            return null;
        }
    }
}
