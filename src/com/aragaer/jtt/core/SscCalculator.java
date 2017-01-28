// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;


public class SscCalculator {
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

	private Interval getDayIntervalForJDN(long jdn) {
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
        long jdn = longToJDN(now);
	    /* fill 4 transitions at once */
	    final long tr[] = new long[] {
            getDayIntervalForJDN(jdn - 1).end,
            getDayIntervalForJDN(jdn).start,
            getDayIntervalForJDN(jdn).end,
            getDayIntervalForJDN(jdn + 1).start
	    };
	    boolean is_day = true;

	    // if tr2 is before now
	    while (now >= tr[2]) {
            for (int i = 0; i < 3; i++)
                tr[i] = tr[i + 1];
            if (is_day)
                tr[3] = getDayIntervalForJDN(jdn + 1).end;
            else {
                jdn++;
                tr[3] = getDayIntervalForJDN(jdn + 1).start;
            }
            is_day = !is_day;
	    }

	    // (else) if tr1 is after now
	    while (now < tr[1]) {
            for (int i = 0; i < 3; i++)
                tr[i + 1] = tr[i];
            if (is_day)
                tr[0] = getDayIntervalForJDN(jdn - 1).start;
            else {
                jdn--;
                tr[0] = getDayIntervalForJDN(jdn - 1).end;
            }
            is_day = !is_day;
	    }

	    return new ThreeIntervals(tr, is_day);
	}

    public static final long ms_per_day = TimeUnit.SECONDS.toMillis(60 * 60 * 24);

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
