// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import java.util.concurrent.TimeUnit;


public class SscCalculator implements IntervalProvider {
    private static SscCalculator instance;

    public static SscCalculator getInstance(IntervalCalculator calculator) {
        if (instance == null)
            instance = new SscCalculator(calculator);
        return instance;
    }

    private IntervalCalculator _calculator;

    private SscCalculator(IntervalCalculator calculator) {
        _calculator = calculator;
    }

    public ThreeIntervals getIntervalsForTimestamp(long now) {
        IntervalBuilder builder = new IntervalBuilder(longToJDN(now), _calculator);

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
}
