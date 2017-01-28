// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class HourTest {

    @Test public void testCreate() {
        Hour hour = new Hour(0);
        assertEquals(hour.num, 0);
        assertEquals(hour.quarter, 0);
        assertEquals(hour.tick, 0);
    }

    @Test public void testBoundary() {
        // There are 6 hour boundaries per interval
        // lower/upperBoundary methods give the number of upper and lower boundary of given hour
        // getHourBoundary method calculates the timestamp of boundary within interval
        for (int i = 0; i < 12; i++) {
            assertEquals(Hour.lowerBoundary(i), (i+5) % 6); // Hour 0 starts at boundary 5 of previous interval
            assertEquals(Hour.upperBoundary(i), i % 6);
            assertEquals(Hour.getHourBoundary(0, 60, i), 5+i*10);
        }
    }

    @Test public void testCompare() {
        Hour h1 = new Hour(0, 0, 0);
        Hour h2 = new Hour(0, 0, 0);
        Hour h3 = new Hour(1, 0, 0);
        Hour h4 = new Hour(0, 1, 0);
        Hour h5 = new Hour(0, 0, 1);

        assertEquals(h1, h1);
        assertNotEquals(h1, null);
        assertNotEquals(h1, this);

        assertEquals(h1, h2);
        assertEquals(h1.hashCode(), h2.hashCode());
        assertNotEquals(h1, h3);
        assertNotEquals(h1, h4);
        assertNotEquals(h1, h5);
        assertNotEquals(h1.hashCode(), h3.hashCode());
        assertNotEquals(h1.hashCode(), h4.hashCode());
        assertNotEquals(h1.hashCode(), h5.hashCode());
    }

    @Test public void testTickNumber() {
        assertEquals(Hour.fromTickNumber(0), new Hour(0, 2, 0)); // Tick 0 of day is middle of hour 0
        assertEquals(Hour.fromTickNumber(1), new Hour(0, 2, 1));
        assertEquals(Hour.fromTickNumber(Hour.TICKS_PER_QUARTER), new Hour(0, 3, 0));
        assertEquals(Hour.fromTickNumber(Hour.TICKS_PER_HOUR-Hour.TICKS_PER_QUARTER),
                     new Hour(1, 1, 0));
        assertEquals(Hour.fromTickNumber(Hour.TICKS_PER_DAY-2*Hour.TICKS_PER_QUARTER-3),
                     new Hour(11, 3, Hour.TICKS_PER_QUARTER-3));
    }

    @Test public void testCreateFromTickNumberRounded() {
        Hour h1 = new Hour(1, 2, 3);
        Hour h2 = new Hour(1, 2, 7);
        assertEquals(Hour.fromTickNumber(h1.wrapped, Hour.TICKS_PER_QUARTER/2), new Hour(1, 2, 0));
        assertEquals(Hour.fromTickNumber(h2.wrapped, Hour.TICKS_PER_QUARTER/2), new Hour(1, 2, 5));
        assertEquals(Hour.fromTickNumber(h1.wrapped, Hour.TICKS_PER_QUARTER), new Hour(1, 2, 0));
        assertEquals(Hour.fromTickNumber(h2.wrapped, Hour.TICKS_PER_QUARTER), new Hour(1, 2, 0));
    }

    @Test public void testCreateFromInterval() {
        Interval day = new Interval(0, 240, true);
        Interval night = new Interval(1000, 1240, false);
        assertEquals(Hour.fromInterval(day, 0), new Hour(6, 2, 0));
        assertEquals(Hour.fromInterval(day, 23), new Hour(7, 0, 3));
        assertEquals(Hour.fromInterval(night, 1111), new Hour(3, 1, 1));
    }
}
