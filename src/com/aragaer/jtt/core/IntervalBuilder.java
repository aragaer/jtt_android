// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;


/* protected */ class IntervalBuilder {
    private final IntervalCalculator _calculator;
    private long _jdn;
    private Interval _current;

    IntervalBuilder(long jdn, IntervalCalculator calculator) {
        _jdn = jdn;
        _calculator = calculator;
        _current = _calculator.getDayIntervalForJDN(jdn);
    }

    Interval getMiddleInterval() {
        return _current;
    }

    private Interval getPreviousInterval() {
        if (_current.is_day)
            return new Interval(_calculator.getDayIntervalForJDN(_jdn-1).end,
                                _current.start, false);
        else
            return _calculator.getDayIntervalForJDN(_jdn-1);
    }

    private Interval getNextInterval() {
        if (_current.is_day)
            return new Interval(_current.end, _calculator.getDayIntervalForJDN(_jdn+1).start, false);
        else
            return _calculator.getDayIntervalForJDN(_jdn);
    }

    void slideToPrevious() {
        _current = getPreviousInterval();
        if (_current.is_day)
            _jdn--;
    }

    void slideToNext() {
        _current = getNextInterval();
        if (!_current.is_day)
            _jdn++;
    }

    ThreeIntervals getThreeIntervals() {
        return new ThreeIntervals(getPreviousInterval(), _current, getNextInterval());
    }
}
