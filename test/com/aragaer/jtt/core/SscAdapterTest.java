// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import static org.junit.Assert.*;

import com.aragaer.jtt.core.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.*;


public class SscAdapterTest {

    private SscAdapter _calculator;

    @Before public void setUp() {
        _calculator = SscAdapter.getInstance();
    }

    @Test public void testIsSingleton() {
        assertTrue(_calculator == SscAdapter.getInstance());
    }

    @Test public void testCache() {
        Interval interval = _calculator.getDayIntervalForJDN(0);
        assertTrue(interval == _calculator.getDayIntervalForJDN(0));
    }

    @Test public void testSunriseLondon01Jan2000() {
        int offsetMillis = (int) TimeUnit.MINUTES.toMillis(0);
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getAvailableIDs(offsetMillis)[0]);
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(2000, 0, 1, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        _calculator.setLocation(51.5f, 0f);

        Calendar sunrise = _calculator.getSunriseFor(calendar);

        Calendar expected = Calendar.getInstance(tz);
        expected.set(2000, 0, 1, 8, 6, 0);
        expected.set(Calendar.MILLISECOND, 0);
        assertEquals(sunrise.getTimeInMillis(), expected.getTimeInMillis());
    }


    @Test public void testSunsetCapeVerder01Jan2000() {
        int offsetMillis = (int) TimeUnit.MINUTES.toMillis(-60);
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getAvailableIDs(offsetMillis)[0]);
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(2000, 0, 1, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        _calculator.setLocation(15.11f, -23.6f);

        Calendar sunset = _calculator.getSunsetFor(calendar);

        Calendar expected = Calendar.getInstance(tz);
        expected.set(2000, 0, 1, 18, 16, 0);
        expected.set(Calendar.MILLISECOND, 0);
        assertEquals(sunset.getTimeInMillis(), expected.getTimeInMillis());
    }

    @Test public void testLondonDay01Jan2000() {
        int offsetMillis = (int) TimeUnit.MINUTES.toMillis(0);
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getAvailableIDs(offsetMillis)[0]);
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(2000, 0, 1, 12, 0, 0); // Need it to be at least noon
        calendar.set(Calendar.MILLISECOND, 0);
        long timestamp = calendar.getTimeInMillis();
        long jdn = Jdn.fromTimestamp(timestamp);
        assertEquals("JDN", jdn, 2451545);

        _calculator.setLocation(51.5f, 0f);
        Interval interval = _calculator.getDayIntervalForJDN(jdn);

        assertTrue(interval.is_day);

        assertTrue(interval.start < timestamp);
        Calendar start = Calendar.getInstance(tz);
        start.setTimeInMillis(interval.start);
        assertEquals("Hour of sunrise in London on 1 Jan of 2000", start.get(Calendar.HOUR_OF_DAY), 8);
        assertEquals("Minute of sunrise in London on 1 Jan of 2000", start.get(Calendar.MINUTE), 6);
        assertEquals(start.get(Calendar.SECOND), 0);
        assertEquals(start.get(Calendar.MILLISECOND), 0);

        assertTrue(interval.end > timestamp);
        Calendar end = Calendar.getInstance(tz);
        end.setTimeInMillis(interval.end);
        assertEquals("Hour of sunset in London on 1 Jan of 2000", end.get(Calendar.HOUR_OF_DAY), 16);
        assertEquals("Minute of sunset in London on 1 Jan of 2000", end.get(Calendar.MINUTE), 2);
        assertEquals(end.get(Calendar.SECOND), 0);
        assertEquals(end.get(Calendar.MILLISECOND), 0);
    }

    @Test public void testMoscowDay22Jun2014() {
        int offsetMillis = (int) TimeUnit.MINUTES.toMillis(180);
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getAvailableIDs(offsetMillis)[0]);
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(2014, 5, 22, 15, 0, 0); // Need it to be at least noon in London
        calendar.set(Calendar.MILLISECOND, 0);
        long timestamp = calendar.getTimeInMillis();
        long jdn = Jdn.fromTimestamp(timestamp);

        _calculator.setLocation(55.93f, 37.79f);
        Interval interval = _calculator.getDayIntervalForJDN(jdn);

        assertTrue(interval.is_day);

        assertTrue(interval.start < timestamp);
        Calendar start = Calendar.getInstance(tz);
        start.setTimeInMillis(interval.start);
        assertEquals("Hour of sunrise in Moscow on 22 Jun of 2014", start.get(Calendar.HOUR_OF_DAY), 3);
        assertEquals("Minute of sunrise in Moscow on 22 Jun of 2014", start.get(Calendar.MINUTE), 43);
        assertEquals(start.get(Calendar.SECOND), 0);
        assertEquals(start.get(Calendar.MILLISECOND), 0);

        assertTrue(interval.end > timestamp);
        Calendar end = Calendar.getInstance(tz);
        end.setTimeInMillis(interval.end);
        assertEquals("Hour of sunset in Moscow on 22 Jun of 2014", end.get(Calendar.HOUR_OF_DAY), 21);
        assertEquals("Minute of sunset in Moscow on 22 Jun of 2014", end.get(Calendar.MINUTE), 19);
        assertEquals(end.get(Calendar.SECOND), 0);
        assertEquals(end.get(Calendar.MILLISECOND), 0);
    }
}
