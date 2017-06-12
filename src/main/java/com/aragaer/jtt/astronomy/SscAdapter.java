// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;


public  class SscAdapter implements SolarEventCalculator {
    private final LocationHandler _locationHandler;

    public SscAdapter(LocationHandler locationHandler) {
        _locationHandler = locationHandler;
    }

    @Override public Calendar getSunriseFor(Calendar noon) {
        float[] location = _locationHandler.getLocation();
        SunriseSunsetCalculator calculator = getCalculatorForLocation(location[0], location[1]);
        return calculator.getOfficialSunriseCalendarForDate((Calendar) noon.clone());
    }

    @Override public Calendar getSunsetFor(Calendar noon) {
        float[] location = _locationHandler.getLocation();
        SunriseSunsetCalculator calculator = getCalculatorForLocation(location[0], location[1]);
        return calculator.getOfficialSunsetCalendarForDate((Calendar) noon.clone());
    }

    private SunriseSunsetCalculator getCalculatorForLocation(float latitude, float longitude) {
        int offsetMillis = (int) TimeUnit.HOURS.toMillis(Math.round(longitude/15));
        return new SunriseSunsetCalculator(new Location(latitude, longitude),
                                           getTimeZone(offsetMillis));
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
