// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;

import java.util.Calendar;
import java.util.TimeZone;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;


public  class SscAdapter implements SolarEventCalculator {
    private static SscAdapter instance;

    public static SscAdapter getInstance() {
        if (instance == null)
            instance = new SscAdapter();
        return instance;
    }

    private SunriseSunsetCalculator _calculator;

    private SscAdapter() {
        setLocation(0, 0);
    }

    @Override public Calendar getSunriseFor(Calendar noon) {
        return _calculator.getOfficialSunriseCalendarForDate((Calendar) noon.clone());
    }

    @Override public Calendar getSunsetFor(Calendar noon) {
        return _calculator.getOfficialSunsetCalendarForDate((Calendar) noon.clone());
    }

    public void setLocation(float latitude, float longitude) {
        _calculator = new SunriseSunsetCalculator(new Location(latitude, longitude),
                                                  TimeZone.getDefault());
    }
}
