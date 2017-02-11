// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.aragaer.jtt.core.test.TestIntervalCalculator;


public class IntervalBuilderTest {
    static final long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);


    private TestIntervalCalculator _calculator = new TestIntervalCalculator();
    private IntervalBuilder _builder;
    private long _noon;

    @Before public void setUp() {
        _builder = new IntervalBuilder(0, _calculator);
        _noon = MS_PER_DAY/2 - TimeZone.getDefault().getOffset(0);
    }

    @Test public void testCreate() {
        Interval middle = _builder.getMiddleInterval();
        assertEquals(_builder.getThreeIntervals(),
                     new ThreeIntervals(new long[] { _noon - MS_PER_DAY*3/4, _noon - MS_PER_DAY/4,
                                                     _noon + MS_PER_DAY/4, _noon + MS_PER_DAY*3/4 }, true));
    }

    @Test public void testSlideForward() {
        _builder.slideToNext();

        assertEquals(_builder.getThreeIntervals(),
                     new ThreeIntervals(new long[] { _noon - MS_PER_DAY/4, _noon + MS_PER_DAY/4,
                                                     _noon + MS_PER_DAY*3/4, _noon + MS_PER_DAY*5/4 }, false));

        _builder.slideToNext();

        assertEquals(_builder.getThreeIntervals(),
                     new ThreeIntervals(new long[] { _noon + MS_PER_DAY/4, _noon + MS_PER_DAY*3/4,
                                                     _noon + MS_PER_DAY*5/4, _noon + MS_PER_DAY*7/4 }, true));
    }

    @Test public void testSlideBack() {
        _builder.slideToPrevious();

        assertEquals(_builder.getThreeIntervals(),
                     new ThreeIntervals(new long[] { _noon - MS_PER_DAY*5/4, _noon - MS_PER_DAY*3/4,
                                                     _noon - MS_PER_DAY/4, _noon + MS_PER_DAY/4 }, false));

        _builder.slideToPrevious();

        assertEquals(_builder.getThreeIntervals(),
                     new ThreeIntervals(new long[] { _noon - MS_PER_DAY*7/4, _noon - MS_PER_DAY*5/4,
                                                     _noon - MS_PER_DAY*3/4, _noon - MS_PER_DAY/4 }, true));
    }
}
