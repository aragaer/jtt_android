package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalEndObserver;
import com.aragaer.jtt.astronomy.TestDayIntervalService;
import static com.aragaer.jtt.core.JttTime.TICKS_PER_DAY;
import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;


public class ClockTest {

    private Clock clock;
    private TestMetronome metronome;
    private TestDayIntervalService astrolabe;
    private TestChime chime;
    private Cogs cogs;

    @Before public void setUp() {
        TestModule module = new TestModule();
        metronome = (TestMetronome) module.getMetronome();
        chime = (TestChime) module.getChime();
        clock = new Clock(chime, metronome);
        astrolabe = new TestDayIntervalService();
        clock.bindToDayIntervalService(astrolabe);
        clock.registerIntervalEndObserver(astrolabe);
        cogs = clock.getCogs();
    }

    @Test public void shouldStartMetronomeBasedOnCurrentInterval() {
        DayInterval interval = DayInterval.Day(10, 10 + TICKS_PER_INTERVAL * 5);

        clock.setInterval(interval);

        assertThat(metronome.start, equalTo(10L));
        assertThat(metronome.tickLength, equalTo(5L));
    }

    @Test public void shouldNotifyDayIntervalServiceOnIntervalEnd() {
        long dayStart = 10;
        long dayTickLength = 5;
        long dayEnd = dayStart + dayTickLength * TICKS_PER_INTERVAL;
        clock.setInterval(DayInterval.Day(dayStart, dayEnd));

        cogs.rotate(5);

        assertThat("metronome start at sunrise", metronome.start, equalTo(dayStart));
        assertThat("cogs.rotate length", metronome.tickLength, equalTo(dayTickLength));

        long nightStart = dayEnd;
        long nightTickLength = 2;
        long nightEnd = nightStart + nightTickLength * TICKS_PER_INTERVAL;

        astrolabe.setNextResult(DayInterval.Night(nightStart, nightEnd));

        cogs.rotate(TICKS_PER_INTERVAL);

        assertThat(metronome.start, equalTo(nightStart));
        assertThat(metronome.tickLength, equalTo(nightTickLength));
    }

    @Test public void shouldUseDayTime() {
        int tickNumber = 42;

        DayInterval interval = DayInterval.Day(0, 1);
        clock.setInterval(interval);
        cogs.rotate(tickNumber);

        assertThat("chime number", chime.getLastTick(), equalTo(tickNumber + TICKS_PER_INTERVAL));
    }

    @Test public void shouldSwitchIntervals() {
        long night1TickLength = 2;
        long day1TickLength = 5;
        long night2TickLength = 3;
        long day2TickLength = 6;
        long sunset1 = 10;
        long sunrise1 = sunset1 + night1TickLength * TICKS_PER_INTERVAL;
        long sunset2 = sunrise1 + day1TickLength * TICKS_PER_INTERVAL;
        long sunrise2 = sunset2 + night2TickLength * TICKS_PER_INTERVAL;
        int lastTick = 0;
        int tickCount;

        clock.setInterval(DayInterval.Night(sunset1, sunrise1));
        astrolabe.setNextResult(DayInterval.Day(sunrise1, sunset2));

        tickCount = 2;
        lastTick += tickCount;
        cogs.rotate(tickCount);
        assertThat("chime number", chime.getLastTick(), equalTo(lastTick));
        assertThat(metronome.tickLength, equalTo(night1TickLength));

        tickCount = 50;
        lastTick += tickCount;
        cogs.rotate(tickCount);
        assertThat("chime number", chime.getLastTick(), equalTo(lastTick));

        tickCount = TICKS_PER_INTERVAL-53;
        lastTick += tickCount;
        cogs.rotate(tickCount);
        assertThat("chime number", chime.getLastTick(), equalTo(lastTick));
        assertThat(metronome.tickLength, equalTo(night1TickLength));

        tickCount = 1;
        lastTick += tickCount;
        cogs.rotate(tickCount + 2);
        assertThat("chime number ignores overrun", chime.getLastTick(), equalTo(lastTick));
        assertThat(metronome.tickLength, equalTo(day1TickLength));

        astrolabe.setNextResult(DayInterval.Night(sunset2, sunrise2));

        tickCount = 20;
        lastTick += tickCount;
        cogs.rotate(tickCount);
        assertThat("chime number", chime.getLastTick(), equalTo(lastTick));
        assertThat(metronome.tickLength, equalTo(day1TickLength));

        tickCount = TICKS_PER_INTERVAL-20;
        lastTick += tickCount;
        cogs.rotate(tickCount + 10);
        assertThat("chime number ignores overrun", chime.getLastTick(), equalTo(0));
        assertThat(metronome.tickLength, equalTo(night2TickLength));
    }

    @Test public void shouldStartMetronomeWhenIntervalChanged() {
        long dayTickLength = 300;
        int dayTicksPassed = 42;
        long dayStartOffset = -dayTickLength * dayTicksPassed;
        long dayEndOffset = dayStartOffset + dayTickLength * TICKS_PER_INTERVAL;
        long now = System.currentTimeMillis();
        DayInterval day = DayInterval.Day(now + dayStartOffset, now + dayEndOffset);

        clock.setInterval(day);

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
