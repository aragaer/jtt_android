// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.aragaer.jtt.core.test.TestIntervalCalculator;


public class SscCalculatorTest {
    static final long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);

    private TestIntervalCalculator _intervalCalculator;
    private SscCalculator _calculator;

    @Before public void setUp() {
        _intervalCalculator = new TestIntervalCalculator();
        _calculator = new SscCalculator(_intervalCalculator);
    }

    @Test public void testCurrentDayAfterNoon() {
        long testTime = MS_PER_DAY*5/8 - TimeZone.getDefault().getOffset(0);

        Calendar noon = Calendar.getInstance();
        noon.setTimeInMillis(MS_PER_DAY/2 - TimeZone.getDefault().getOffset(testTime));
        long curSunrise = _intervalCalculator.getSunriseFor(noon).getTimeInMillis();
        long curSunset = _intervalCalculator.getSunsetFor(noon).getTimeInMillis();
        noon.add(Calendar.DATE, -1);
        long prevSunset = _intervalCalculator.getSunsetFor(noon).getTimeInMillis();
        noon.add(Calendar.DATE, 2);
        long nextSunrise = _intervalCalculator.getSunriseFor(noon).getTimeInMillis();

        ThreeIntervals result = _calculator.getIntervalsForTimestamp(testTime);

        assertEquals(result.getTransitions()[0], prevSunset);
        assertEquals(result.getTransitions()[1], curSunrise);
        assertEquals(result.getTransitions()[2], curSunset);
        assertEquals(result.getTransitions()[3], nextSunrise);
        assertEquals(result.isDay(), true);
    }

    @Test public void testCurrentDayBeforeNoon() {
        long testTime = MS_PER_DAY*3/8 - TimeZone.getDefault().getOffset(0);

        Calendar noon = Calendar.getInstance();
        noon.setTimeInMillis(MS_PER_DAY/2 - TimeZone.getDefault().getOffset(testTime));
        long curSunrise = _intervalCalculator.getSunriseFor(noon).getTimeInMillis();
        long curSunset = _intervalCalculator.getSunsetFor(noon).getTimeInMillis();
        noon.add(Calendar.DATE, -1);
        long prevSunset = _intervalCalculator.getSunsetFor(noon).getTimeInMillis();
        noon.add(Calendar.DATE, 2);
        long nextSunrise = _intervalCalculator.getSunriseFor(noon).getTimeInMillis();

        ThreeIntervals result = _calculator.getIntervalsForTimestamp(testTime);

        assertEquals(result.getTransitions()[0], prevSunset);
        assertEquals(result.getTransitions()[1], curSunrise);
        assertEquals(result.getTransitions()[2], curSunset);
        assertEquals(result.getTransitions()[3], nextSunrise);
        assertEquals(result.isDay(), true);
    }

    @Test public void testCurrentNightAfterMidnight() {
        long testTime = - TimeZone.getDefault().getOffset(0) - 1;

        Calendar noon = Calendar.getInstance();
        noon.setTimeInMillis(MS_PER_DAY/2 - TimeZone.getDefault().getOffset(testTime));
        long curSunrise = _intervalCalculator.getSunriseFor(noon).getTimeInMillis();
        long curSunset = _intervalCalculator.getSunsetFor(noon).getTimeInMillis();
        noon.add(Calendar.DATE, -1);
        long prevSunrise = _intervalCalculator.getSunriseFor(noon).getTimeInMillis();
        long prevSunset = _intervalCalculator.getSunsetFor(noon).getTimeInMillis();

        ThreeIntervals result = _calculator.getIntervalsForTimestamp(testTime);

        assertEquals(result.getTransitions()[0], prevSunrise);
        assertEquals(result.getTransitions()[1], prevSunset);
        assertEquals(result.getTransitions()[2], curSunrise);
        assertEquals(result.getTransitions()[3], curSunset);
        assertEquals(result.isDay(), false);
    }

    @Test public void testCurrentNightAfterSunset() {
        long testTime = MS_PER_DAY*3/4 - TimeZone.getDefault().getOffset(0) + 1;

        Calendar noon = Calendar.getInstance();
        noon.setTimeInMillis(MS_PER_DAY/2 - TimeZone.getDefault().getOffset(testTime));
        long curSunrise = _intervalCalculator.getSunriseFor(noon).getTimeInMillis();
        long curSunset = _intervalCalculator.getSunsetFor(noon).getTimeInMillis();
        noon.add(Calendar.DATE, +1);
        long nextSunrise = _intervalCalculator.getSunriseFor(noon).getTimeInMillis();
        long nextSunset = _intervalCalculator.getSunsetFor(noon).getTimeInMillis();

        ThreeIntervals result = _calculator.getIntervalsForTimestamp(testTime);

        assertEquals(result.getTransitions()[0], curSunrise);
        assertEquals(result.getTransitions()[1], curSunset);
        assertEquals(result.getTransitions()[2], nextSunrise);
        assertEquals(result.getTransitions()[3], nextSunset);
        assertEquals(result.isDay(), false);
    }
}
