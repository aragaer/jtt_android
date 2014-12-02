package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import java.lang.Thread;

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class ClockTickCallbackTest {

    private TestClock clock;

    @Before
    public void setUp() {
        clock = new TestClock();
    }

    @Test
    public void shouldTrigger0TicksIfLessThanLength() {
        long now = System.currentTimeMillis();
        ClockTickCallback callback = new ClockTickCallback(clock, now, 10000);
        callback.onTick();
        assertThat(clock.ticks, equalTo(0));
    }

    @Test
    public void shouldTriggerRequiredNumberOfTicks() {
        long offset = 1000 * 42 + 250;
        long now = System.currentTimeMillis();
        ClockTickCallback callback = new ClockTickCallback(clock, now - offset, 1000);
        callback.onTick();
        assertThat(clock.ticks, equalTo(42));
    }

    @Test
    public void shouldTickOnceAgain() throws InterruptedException {
        long offset = 1000 * 42 + 997;
        long now = System.currentTimeMillis();
        ClockTickCallback callback = new ClockTickCallback(clock, now - offset, 1000);
        callback.onTick();
        assertThat(clock.ticks, equalTo(42));
        Thread.sleep(5);
        callback.onTick();
        assertThat(clock.ticks, equalTo(43));
    }
}
