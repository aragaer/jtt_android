// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;


public class SscCalculator implements IntervalCalculator {
    private SunriseSunsetCalculator _calculator;
    private final Map<Long, Interval> cache = new HashMap<Long, Interval>();

    public SscCalculator() {
        setLocation(0, 0);
    }

    public void setLocation(float latitude, float longitude) {
        _calculator = new SunriseSunsetCalculator(new Location(latitude, longitude),
                                                  TimeZone.getDefault());
        cache.clear();
    }

    @Override public Interval getDayIntervalForJDN(long jdn) {
        Interval result = cache.get(jdn);
        if (result == null) {
            final Calendar date = Calendar.getInstance();
            date.setTimeInMillis(JDToLong(jdn));
            result = new Interval(_calculator.getOfficialSunriseCalendarForDate(date).getTimeInMillis(),
                                  _calculator.getOfficialSunsetCalendarForDate(date).getTimeInMillis(),
                                  true);
            cache.put(jdn, result);
        }
        return result;
    }

    public ThreeIntervals getSurroundingIntervalsForTimestamp(long now) {
        IntervalBuilder builder = new IntervalBuilder(longToJDN(now), this);
        long timestamp;

        // if it is past sunset
        while (now >= builder.getMiddleInterval().end)
            builder.slideToNext();

        // (else) if it is before sunrise
        while (now < builder.getMiddleInterval().start)
            builder.slideToPrevious();

        return builder.getThreeIntervals();
    }

    private static final long ms_per_day = TimeUnit.SECONDS.toMillis(60 * 60 * 24);

    private static long longToJDN(long time) {
        return (long) Math.floor(longToJD(time));
    }

    private static double longToJD(long time) {
        return time / ((double) ms_per_day) + 2440587.5;
    }

    private static long JDToLong(final double jd) {
        return Math.round((jd - 2440587.5) * ms_per_day);
    }
}
