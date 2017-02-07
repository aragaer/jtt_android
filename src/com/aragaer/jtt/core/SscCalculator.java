// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;


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
        IntervalBuilder builder = new IntervalBuilder(Jdn.fromTimestamp(now), _calculator);

        // if it is past sunset
        while (now >= builder.getMiddleInterval().end)
            builder.slideToNext();

        // (else) if it is before sunrise
        while (now < builder.getMiddleInterval().start)
            builder.slideToPrevious();

        return builder.getThreeIntervals();
    }
}
