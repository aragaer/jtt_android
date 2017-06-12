// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core.test;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.aragaer.jtt.astronomy.SolarEventCalculator;
import com.aragaer.jtt.core.Interval;


public class TestIntervalCalculator implements SolarEventCalculator {
    static final long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);

    @Override public Calendar getSunriseFor(Calendar noon) {
        long millis = noon.getTimeInMillis();
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(millis - MS_PER_DAY/4);
        return result;
    }

    @Override public Calendar getSunsetFor(Calendar noon) {
        long millis = noon.getTimeInMillis();
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(millis + MS_PER_DAY/4);
        return result;
    }
}
