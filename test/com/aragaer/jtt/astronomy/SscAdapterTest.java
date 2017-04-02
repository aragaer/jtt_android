// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;


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
        verifyEquals(sunrise, 2000, 0, 1, 8, 6, tz);
    }


    @Test public void testSunsetCapeVerde01Jan2000() {
        int offsetMillis = (int) TimeUnit.MINUTES.toMillis(-60);
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getAvailableIDs(offsetMillis)[0]);
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(2000, 0, 1, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        _calculator.setLocation(15.11f, -23.6f);

        Calendar sunset = _calculator.getSunsetFor(calendar);
        verifyEquals(sunset, 2000, 0, 1, 18, 16, tz);
    }

    @Test public void testLondonDay01Jan2000() {
        int offsetMillis = (int) TimeUnit.MINUTES.toMillis(0);
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getAvailableIDs(offsetMillis)[0]);
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(2000, 0, 1, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        _calculator.setLocation(51.5f, 0f);

        Calendar sunrise = _calculator.getSunriseFor(calendar);
        verifyEquals(sunrise, 2000, 0, 1, 8, 6, tz);

        Calendar sunset = _calculator.getSunsetFor(calendar);
        verifyEquals(sunset, 2000, 0, 1, 16, 2, tz);
    }

    @Test public void testMoscowDay22Jun2014() {
        int offsetMillis = (int) TimeUnit.MINUTES.toMillis(180);
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getAvailableIDs(offsetMillis)[0]);
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(2014, 5, 22, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        _calculator.setLocation(55.93f, 37.79f);

        Calendar sunrise = _calculator.getSunriseFor(calendar);
        verifyEquals(sunrise, 2014, 5, 22, 3, 43, tz);

        Calendar sunset = _calculator.getSunsetFor(calendar);
        verifyEquals(sunset, 2014, 5, 22, 21, 19, tz);
    }

    @Test public void testReykjavikSummerSolstice2017() {
        int offsetMillis = (int) TimeUnit.MINUTES.toMillis(0);
        TimeZone tz = getTimeZone(offsetMillis);
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(2017, 5, 22, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        _calculator.setLocation(64.13f, -21.9f);

        Calendar sunrise = _calculator.getSunriseFor(calendar);
        verifyEquals(sunrise, 2017, 5, 22, 2, 55, tz);

        // Note - next day, after midnight
        Calendar sunset = _calculator.getSunsetFor(calendar);
        verifyEquals(sunset, 2017, 5, 23, 0, 4, tz);
    }

    static void verifyEquals(Calendar actual, int year, int month,
                             int day, int hour, int minute, TimeZone tz) {
        Calendar expected = Calendar.getInstance(tz);
        expected.set(year, month, day, hour, minute, 0);
        expected.set(Calendar.MILLISECOND, 0);
        assertEquals(actual.getTimeInMillis(), expected.getTimeInMillis());
    }

    static TimeZone getTimeZone(int offsetMillis) {
        for (String tzName : TimeZone.getAvailableIDs(offsetMillis)) {
            TimeZone tz = TimeZone.getTimeZone(tzName);
            if (!tz.useDaylightTime())
                return tz;
        }
        throw new RuntimeException("Failed to find timezone for offset "+offsetMillis);
    }
}
