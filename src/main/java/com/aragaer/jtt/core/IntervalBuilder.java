// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.aragaer.jtt.astronomy.SolarEventCalculator;


/* protected */ class IntervalBuilder {
    static final long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);

    private final SolarEventCalculator _calculator;
    private Interval _current;
    private final Calendar _noon;

    IntervalBuilder(long timestamp, SolarEventCalculator calculator) {
        _calculator = calculator;
        _noon = Calendar.getInstance();
        long tzOffset = TimeZone.getDefault().getOffset(timestamp);
        timestamp -= timestamp % MS_PER_DAY;
        _noon.setTimeInMillis(timestamp + MS_PER_DAY/2 - tzOffset);
        long sunrise = _calculator.getSunriseFor(_noon).getTimeInMillis();
        long sunset = _calculator.getSunsetFor(_noon).getTimeInMillis();
        _current = new Interval(sunrise, sunset, true);
    }

    Interval getMiddleInterval() {
        return _current;
    }

    private Interval getPreviousInterval() {
        Calendar prevNoon = (Calendar) _noon.clone();
        prevNoon.add(Calendar.DATE, -1);
        if (_current.is_day)
            return new Interval(_calculator.getSunsetFor(prevNoon).getTimeInMillis(),
                                _current.start, false);
        else
            return new Interval(_calculator.getSunriseFor(prevNoon).getTimeInMillis(),
                                _calculator.getSunsetFor(prevNoon).getTimeInMillis(), true);
    }

    private Interval getNextInterval() {
        Calendar nextNoon = (Calendar) _noon.clone();
        nextNoon.add(Calendar.DATE, 1);
        if (_current.is_day)
            return new Interval(_current.end,
                                _calculator.getSunriseFor(nextNoon).getTimeInMillis(), false);
        else
            return new Interval(_calculator.getSunriseFor(_noon).getTimeInMillis(),
                                _calculator.getSunsetFor(_noon).getTimeInMillis(), true);
    }

    void slideToPrevious() {
        _current = getPreviousInterval();
        if (_current.is_day)
            _noon.add(Calendar.DATE, -1);
    }

    void slideToNext() {
        _current = getNextInterval();
        if (!_current.is_day)
            _noon.add(Calendar.DATE, 1);
    }

    ThreeIntervals getThreeIntervals() {
        return new ThreeIntervals(getPreviousInterval(), _current, getNextInterval());
    }
}
