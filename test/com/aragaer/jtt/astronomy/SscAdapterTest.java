// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;

import static org.junit.Assert.*;

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
        calendar.set(2000, 0, 1, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        _calculator.setLocation(51.5f, 0f);

        Calendar sunrise = _calculator.getSunriseFor(calendar);

        Calendar expectedSunrise = Calendar.getInstance(tz);
        expectedSunrise.set(2000, 0, 1, 8, 6, 0);
        expectedSunrise.set(Calendar.MILLISECOND, 0);
        assertEquals(sunrise.getTimeInMillis(), expectedSunrise.getTimeInMillis());

        Calendar sunset = _calculator.getSunsetFor(calendar);

        Calendar expectedSunset = Calendar.getInstance(tz);
        expectedSunset.set(2000, 0, 1, 16, 2, 0);
        expectedSunset.set(Calendar.MILLISECOND, 0);
        assertEquals(sunset.getTimeInMillis(), expectedSunset.getTimeInMillis());
    }

    @Test public void testMoscowDay22Jun2014() {
        int offsetMillis = (int) TimeUnit.MINUTES.toMillis(180);
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getAvailableIDs(offsetMillis)[0]);
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(2014, 5, 22, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long timestamp = calendar.getTimeInMillis();
        _calculator.setLocation(55.93f, 37.79f);

        Calendar sunrise = _calculator.getSunriseFor(calendar);

        Calendar expectedSunrise = Calendar.getInstance(tz);
        expectedSunrise.set(2014, 5, 22, 3, 43, 0);
        expectedSunrise.set(Calendar.MILLISECOND, 0);
        assertEquals(sunrise.getTimeInMillis(), expectedSunrise.getTimeInMillis());

        Calendar sunset = _calculator.getSunsetFor(calendar);

        Calendar expectedSunset = Calendar.getInstance(tz);
        expectedSunset.set(2014, 5, 22, 21, 19, 0);
        expectedSunset.set(Calendar.MILLISECOND, 0);
        assertEquals(sunset.getTimeInMillis(), expectedSunset.getTimeInMillis());
    }
}
