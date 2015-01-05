package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.astronomy.DayInterval;

import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;


public class TickServiceTest {
    @Test public void shouldStartMetronomeWithNewInterval() {
        TestTickProvider metronome = new TestTickProvider();
        TickService service = new TickService(metronome);

        service.intervalChanged(DayInterval.Day(10, 10 + 5*TICKS_PER_INTERVAL));

        assertThat(metronome.startTime, equalTo(10L));
        assertThat(metronome.tickLength, equalTo(5L));
    }

    @Test public void shouldInformClientsOnMetronomeTicks() {
        TestTickProvider metronome = new TestTickProvider();
        TickService service = new TickService(metronome);
        TestTickClient client = new TestTickClient();

        service.registerClient(client);

        metronome.tick(10);

        assertThat(client.ticks, equalTo(10));
    }

    @Test public void shouldUseDayTime() {
        TestTickProvider metronome = new TestTickProvider();
        TickService service = new TickService(metronome);
        TestTickClient client = new TestTickClient();

        service.registerClient(client);
        service.intervalChanged(DayInterval.Day(10, 20));

        metronome.tick(10);

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
            counter.rotate(ticks);
        }
    }

    private static class TestTickClient implements TickClient {
        public int ticks;
        public void tickChanged(int ticks) {
            this.ticks = ticks;
        }
    }
}
