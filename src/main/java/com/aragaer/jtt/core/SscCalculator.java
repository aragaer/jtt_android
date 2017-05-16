// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import com.aragaer.jtt.astronomy.SolarEventCalculator;


public class SscCalculator implements IntervalProvider {
    private SolarEventCalculator _calculator;

    public SscCalculator(SolarEventCalculator calculator) {
        _calculator = calculator;
    }

    public ThreeIntervals getIntervalsForTimestamp(long now) {
        IntervalBuilder builder = new IntervalBuilder(now, _calculator);

        // if it is past sunset
        while (now >= builder.getMiddleInterval().end)
            builder.slideToNext();

        // (else) if it is before sunrise
        while (now < builder.getMiddleInterval().start)
            builder.slideToPrevious();

        return builder.getThreeIntervals();
    }
}
