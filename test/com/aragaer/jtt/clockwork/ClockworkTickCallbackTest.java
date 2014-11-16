package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import java.lang.Thread;

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class ClockworkTickCallbackTest {

    private ProbeClockwork clockwork;

    @Before
    public void setUp() {
        clockwork = new ProbeClockwork();
    }

    @Test
    public void shouldTrigger0TicksIfLessThanLength() {
        long now = System.currentTimeMillis();
        ClockworkTickCallback callback = new ClockworkTickCallback(clockwork, now, 10000);
        callback.onTick();
        assertThat(clockwork.ticks, equalTo(0));
    }

    @Test
    public void shouldTriggerRequiredNumberOfTicks() {
        long offset = 1000 * 42 + 250;
        long now = System.currentTimeMillis();
        ClockworkTickCallback callback = new ClockworkTickCallback(clockwork, now - offset, 1000);
        callback.onTick();
        assertThat(clockwork.ticks, equalTo(42));
    }

    @Test
    public void shouldTickOnceAgain() throws InterruptedException {
        long offset = 1000 * 42 + 997;
        long now = System.currentTimeMillis();
        ClockworkTickCallback callback = new ClockworkTickCallback(clockwork, now - offset, 1000);
        callback.onTick();
        assertThat(clockwork.ticks, equalTo(42));
        Thread.sleep(5);
        callback.onTick();
        assertThat(clockwork.ticks, equalTo(43));
    }

    private static class ProbeClockwork extends Clockwork {
        int ticks;

        public void rewind() {
            ticks = 0;
        }

        public void tick(int ticks) {
            this.ticks += ticks;
        }
    }
}
