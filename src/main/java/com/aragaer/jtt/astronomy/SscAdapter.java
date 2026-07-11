// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.luckycatlabs.sunrisesunset.Zenith;
import com.luckycatlabs.sunrisesunset.dto.Location;


public  class SscAdapter implements SolarEventCalculator {
    private final LocationHandler _locationHandler;
    private final DayBoundaryHandler _boundaryHandler;

    public SscAdapter(LocationHandler locationHandler, DayBoundaryHandler boundaryHandler) {
        _locationHandler = locationHandler;
        _boundaryHandler = boundaryHandler;
    }

    @Override public Calendar getSunriseFor(Calendar noon) {
        return getCalculator().computeSunriseCalendar(getZenith(), (Calendar) noon.clone());
    }

    @Override public Calendar getSunsetFor(Calendar noon) {
        return getCalculator().computeSunsetCalendar(getZenith(), (Calendar) noon.clone());
    }

    private Zenith getZenith() {
        /* Note: SunriseSunsetCalculator.getSunrise(..., degrees) statics are
         * not used here because they construct Zenith(90 - degrees),
         * contradicting their own javadoc. */
        return new Zenith(90 + _boundaryHandler.getDepression());
    }

    private com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator getCalculator() {
        float[] location = _locationHandler.getLocation();
        int offsetMillis = (int) TimeUnit.HOURS.toMillis(Math.round(location[1]/15));
        return new com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator(
                new Location(location[0], location[1]), getTimeZone(offsetMillis));
    }

    private static TimeZone getTimeZone(int offsetMillis) {
        for (String tzName : TimeZone.getAvailableIDs(offsetMillis)) {
            TimeZone tz = TimeZone.getTimeZone(tzName);
            if (!tz.useDaylightTime())
                return tz;
        }
        throw new RuntimeException("Failed to find timezone for offset "+offsetMillis);
    }
}
