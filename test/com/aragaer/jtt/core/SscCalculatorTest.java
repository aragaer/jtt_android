// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class SscCalculatorTest {
    static final long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);
    static final double UNIX_EPOCH_JD = 2440587.5;

    private TestIntervalCalculator _intervalCalculator;
    private SscCalculator _calculator;

    @Before public void setUp() {
        _intervalCalculator = new TestIntervalCalculator();
        _calculator = SscCalculator.getInstance(_intervalCalculator);
    }

    @Test public void testIsSingleton() {
        assertTrue(_calculator == SscCalculator.getInstance(null));
    }

    @Test public void testCurrentDayAfterNoon() {
        long timestamp = MS_PER_DAY/2;
        ThreeIntervals result = _calculator.getIntervalsForTimestamp(timestamp);
        Interval before = _intervalCalculator.getDayIntervalForJDN((long) UNIX_EPOCH_JD);
        Interval today = _intervalCalculator.getDayIntervalForJDN((long) UNIX_EPOCH_JD+1);
        Interval after = _intervalCalculator.getDayIntervalForJDN((long) UNIX_EPOCH_JD+2);
        assertTrue(today.start <= timestamp);
        assertTrue(today.end > timestamp);
        assertEquals(result.getTransitions()[0], before.end);
        assertEquals(result.getTransitions()[1], today.start);
        assertEquals(result.getTransitions()[2], today.end);
        assertEquals(result.getTransitions()[3], after.start);
        assertEquals(result.isDay(), true);
    }

    @Test public void testCurrentDayBeforeNoon() {
        long timestamp = MS_PER_DAY/2-1;
        ThreeIntervals result = _calculator.getIntervalsForTimestamp(timestamp);
        Interval before = _intervalCalculator.getDayIntervalForJDN((long) UNIX_EPOCH_JD);
        Interval today = _intervalCalculator.getDayIntervalForJDN((long) UNIX_EPOCH_JD+1);
        Interval after = _intervalCalculator.getDayIntervalForJDN((long) UNIX_EPOCH_JD+2);
        assertTrue(today.start <= timestamp);
        assertTrue(today.end > timestamp);
        assertEquals(result.getTransitions()[0], before.end);
        assertEquals(result.getTransitions()[1], today.start);
        assertEquals(result.getTransitions()[2], today.end);
        assertEquals(result.getTransitions()[3], after.start);
        assertEquals(result.isDay(), true);
    }

    @Test public void testCurrentNightAfterMidnight() {
        long timestamp = 0;
        ThreeIntervals result = _calculator.getIntervalsForTimestamp(timestamp);
        Interval today = _intervalCalculator.getDayIntervalForJDN((long) UNIX_EPOCH_JD);
        Interval after = _intervalCalculator.getDayIntervalForJDN((long) UNIX_EPOCH_JD+1);
        assertTrue(today.end <= timestamp);
        assertTrue(after.start > timestamp);
        assertEquals(result.getTransitions()[0], today.start);
        assertEquals(result.getTransitions()[1], today.end);
        assertEquals(result.getTransitions()[2], after.start);
        assertEquals(result.getTransitions()[3], after.end);
        assertEquals(result.isDay(), false);
    }

    @Test public void testCurrentNightAfterSunset() {
        long timestamp = -MS_PER_DAY/4;
        ThreeIntervals result = _calculator.getIntervalsForTimestamp(timestamp);
        Interval today = _intervalCalculator.getDayIntervalForJDN((long) UNIX_EPOCH_JD);
        Interval after = _intervalCalculator.getDayIntervalForJDN((long) UNIX_EPOCH_JD+1);
        assertTrue(today.end <= timestamp);
        assertTrue(after.start > timestamp);
        assertEquals(result.getTransitions()[0], today.start);
        assertEquals(result.getTransitions()[1], today.end);
        assertEquals(result.getTransitions()[2], after.start);
        assertEquals(result.getTransitions()[3], after.end);
        assertEquals(result.isDay(), false);
    }

    private static class TestIntervalCalculator implements IntervalCalculator {
        @Override public Interval getDayIntervalForJDN(long jdn) {
            long jdn_noon = Jdn.toTimestamp(jdn);
            return new Interval(jdn_noon-MS_PER_DAY/4, jdn_noon+MS_PER_DAY/4, true);
        }

        @Override public void setLocation(float latitude, float longitude) {
        }
    }
}
