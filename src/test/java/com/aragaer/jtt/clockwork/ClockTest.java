package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalEndObserver;
import static com.aragaer.jtt.core.JttTime.TICKS_PER_DAY;
import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;


public class ClockTest {

    private Clock clock;
    private TestMetronome metronome;
    private TestChime chime;
    private Cogs cogs;

    @Before public void setUp() {
        metronome = new TestMetronome();
        chime = new TestChime();
        clock = new Clock(chime, metronome);
        cogs = clock.getCogs();
    }

    @Test public void shouldStartMetronomeBasedOnCurrentInterval() {
        DayInterval interval = DayInterval.Day(10, 10 + TICKS_PER_INTERVAL * 5);

        clock.intervalChanged(interval);

        assertThat(metronome.start, equalTo(10L));
        assertThat(metronome.tickLength, equalTo(5L));
    }

    @Test public void shouldNotifyDayIntervalServiceOnIntervalEnd() {
        TestIntervalEndObserver observer = new TestIntervalEndObserver();
        clock.registerIntervalEndObserver(observer);

        long dayStart = 10;
        long dayTickLength = 5;
        long dayEnd = dayStart + dayTickLength * TICKS_PER_INTERVAL;
        clock.intervalChanged(DayInterval.Day(dayStart, dayEnd));

        cogs.rotate(5);

        assertThat("metronome start at sunrise", metronome.start, equalTo(dayStart));
        assertThat("cogs.rotate length", metronome.tickLength, equalTo(dayTickLength));
        assertThat(observer.intervalEndCount, equalTo(0));

        long nightStart = dayEnd;
        long nightTickLength = 2;
        long nightEnd = nightStart + nightTickLength * TICKS_PER_INTERVAL;

        cogs.rotate(TICKS_PER_INTERVAL);

        assertThat(observer.intervalEndCount, equalTo(1));
    }

    @Test public void shouldUseDayTime() {
        int tickNumber = 42;

        DayInterval interval = DayInterval.Day(0, 1);
        clock.intervalChanged(interval);
        cogs.rotate(tickNumber);

        assertThat("chime number", chime.getLastTick(), equalTo(tickNumber + TICKS_PER_INTERVAL));
    }

    @Test public void shouldStartMetronomeWhenIntervalChanged() {
        long dayTickLength = 300;
        int dayTicksPassed = 42;
        long dayStartOffset = -dayTickLength * dayTicksPassed;
        long dayEndOffset = dayStartOffset + dayTickLength * TICKS_PER_INTERVAL;
        long now = System.currentTimeMillis();
        DayInterval day = DayInterval.Day(now + dayStartOffset, now + dayEndOffset);

        clock.intervalChanged(day);

        assertThat(metronome.tickLength, equalTo(dayTickLength));
        assertThat(metronome.start, equalTo(now + dayStartOffset));
        cogs.rotate(dayTicksPassed);
        assertThat(chime.getLastTick(), equalTo(dayTicksPassed+TICKS_PER_INTERVAL));
    }

    @Test public void shouldBindIntervalEndObserverToCogs() {
        TestIntervalEndObserver observer = new TestIntervalEndObserver();
        cogs.rotate(42);
        clock.registerIntervalEndObserver(observer);
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
