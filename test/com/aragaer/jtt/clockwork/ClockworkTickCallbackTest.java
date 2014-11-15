package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class ClockworkTickCallbackTest {

    private ProbeClockwork clockwork;
    private ClockworkTickCallback callback;

    @Before
    public void setUp() {
        clockwork = new ProbeClockwork();
        callback = new ClockworkTickCallback(clockwork);
    }

    @Test
    public void shouldTriggerTick() {
        assertThat(clockwork.ticks, equalTo(0));
        callback.onTick();
        assertThat(clockwork.ticks, equalTo(1));
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
