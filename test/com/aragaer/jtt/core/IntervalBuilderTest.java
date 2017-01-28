// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class IntervalBuilderTest {

    private IntervalCalculator _calculator = new TestIntervalCalculator();
    private IntervalBuilder _builder;

    @Before public void setUp() {
	_builder = new IntervalBuilder(0, _calculator);
    }

    @Test public void testCreate() {
	assertEquals(_builder.getThreeIntervals(),
		     new ThreeIntervals(new long[] { -5, 0, 5, 10 }, true));
    }

    @Test public void testSlideForward() {
	_builder.slideToNext();

	assertEquals(_builder.getThreeIntervals(),
		     new ThreeIntervals(new long[] { 0, 5, 10, 15 }, false));

	_builder.slideToNext();

	assertEquals(_builder.getThreeIntervals(),
		     new ThreeIntervals(new long[] { 5, 10, 15, 20 }, true));
    }

    @Test public void testSlideBack() {
	_builder.slideToPrevious();

	assertEquals(_builder.getThreeIntervals(),
		     new ThreeIntervals(new long[] { -10, -5, 0, 5 }, false));

	_builder.slideToPrevious();

	assertEquals(_builder.getThreeIntervals(),
		     new ThreeIntervals(new long[] { -15, -10, -5, 0 }, true));
    }

    private static class TestIntervalCalculator implements IntervalCalculator {

        @Override public Interval getDayIntervalForJDN(long jdn) {
	    return new Interval(jdn * 10, jdn * 10 + 5, true);
	}
    }
}
