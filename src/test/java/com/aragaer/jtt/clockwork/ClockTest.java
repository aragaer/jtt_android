package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.astronomy.DayInterval;
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
}
