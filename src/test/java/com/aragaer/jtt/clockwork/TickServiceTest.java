package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.astronomy.DayInterval;

import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;


public class TickServiceTest {
    private TestTickProvider metronome;
    private TickService service;
    private TestTickClient client;

    @Before public void setUp() {
        metronome = new TestTickProvider();
        service = new TickService(metronome);
        client = new TestTickClient();
        service.registerClient(client);
    }

    @Test public void shouldStartMetronomeWithNewInterval() {
        service.registerClient(null);

        service.intervalChanged(DayInterval.Day(10, 10 + 5*TICKS_PER_INTERVAL));

        assertThat(metronome.startTime, equalTo(10L));
        assertThat(metronome.tickLength, equalTo(5L));
    }

    @Test public void shouldInformClientsOnMetronomeTicks() {
        metronome.tick(10);

        assertThat(client.ticks, equalTo(10));
    }

    @Test public void shouldUseDayTime() {
        service.intervalChanged(DayInterval.Day(10, 20));

        metronome.tick(10);

        assertThat(client.ticks, equalTo(TICKS_PER_INTERVAL+10));
    }

    @Test public void shouldNotifyClientAboutNewTickCount() {
        service.set(1);

        assertThat(client.ticks, equalTo(1));
    }

    @Test public void shouldNotifyClientAboutNewTickCountDuringDay() {
        service.intervalChanged(DayInterval.Day(10, 20));
        service.set(10);

        assertThat(client.ticks, equalTo(TICKS_PER_INTERVAL+10));
    }

    private static class TestTickProvider implements TickProvider {
        public long startTime, tickLength;
        private TickCounter counter;

        public void attachTo(TickCounter newCounter) {
            counter = newCounter;
        }

        public void start(long start, long tickLength) {
            startTime = start;
            this.tickLength = tickLength;
        }

        public void stop() {
        }

        public void tick(int ticks) {
            counter.set(ticks);
        }
    }

    private static class TestTickClient implements TickClient {
        public int ticks;
        public void tickChanged(int ticks) {
            this.ticks = ticks;
        }
    }
}
