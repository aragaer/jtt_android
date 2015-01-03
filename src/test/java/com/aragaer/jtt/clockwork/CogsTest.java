package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.astronomy.DayIntervalEndObserver;
import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;


public class CogsTest {

    private Cogs cogs;
    private TestChime chime;

    @Before public void setUp() {
        cogs = new Cogs();
        chime = new TestChime();
        cogs.attachChime(chime);
    }

    @Test public void shouldInitializeWithZero() {
        cogs.rotate(0);

        assertThat(chime.getLastTick(), equalTo(0));
    }

    @Test public void shouldPassNewValue() {
        cogs.rotate(1);

        assertThat(chime.getLastTick(), equalTo(1));
    }

    @Test public void shouldAccumulateProgress() {
        cogs.rotate(1);
        cogs.rotate(1);

        assertThat(chime.getLastTick(), equalTo(2));
    }

    @Test public void shouldNotifyOnIntervalEnd() {
        TestIntervalEndObserver observer = new TestIntervalEndObserver();
        cogs.rotate(42);
        cogs.registerIntervalEndObserver(observer);
        assertThat(observer.intervalEndCount, equalTo(0));
        cogs.rotate(TICKS_PER_INTERVAL-42);
        assertThat(observer.intervalEndCount, equalTo(1));
    }

    private static class TestIntervalEndObserver implements DayIntervalEndObserver {
        public int intervalEndCount;
        public void onIntervalEnded() {
            intervalEndCount++;
        }
    }
}
