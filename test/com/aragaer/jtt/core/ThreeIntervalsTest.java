// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import static org.junit.Assert.*;

import org.junit.Test;


public class ThreeIntervalsTest {

    @Test public void testCreate() {
        ThreeIntervals ti = new ThreeIntervals(new long[] {0, 1, 2, 3}, true);
        assertArrayEquals(ti.getTransitions(), new long[] {0, 1, 2, 3});
        assertEquals(ti.isDay(), true);
    }

    @Test public void testCanBeCompared() {
        ThreeIntervals ti1 = new ThreeIntervals(new long[] {0, 1, 2, 3}, true);
        ThreeIntervals ti2 = new ThreeIntervals(new long[] {0, 1, 2, 3}, true);
        ThreeIntervals ti3 = new ThreeIntervals(new long[] {0, 1, 2, 4}, true);
        ThreeIntervals ti4 = new ThreeIntervals(new long[] {0, 1, 2, 3}, false);
        assertEquals(ti1, ti2);
        assertNotEquals(ti1, ti3);
        assertNotEquals(ti1, ti4);
        assertEquals(ti1.hashCode(), ti2.hashCode());
        assertNotEquals(ti1.hashCode(), ti3.hashCode());
        assertNotEquals(ti1.hashCode(), ti4.hashCode());
    }

    @Test public void testCheckInMiddleInterval() {
        ThreeIntervals ti = new ThreeIntervals(new long[] {0, 5, 10, 15}, true);
        assertFalse(ti.surrounds(0));
        assertFalse(ti.surrounds(3));
        assertTrue(ti.surrounds(5));
        assertTrue(ti.surrounds(7));
        assertFalse(ti.surrounds(10));
        assertFalse(ti.surrounds(12));
    }

    @Test public void testMiddleInterval() {
        ThreeIntervals ti = new ThreeIntervals(new long[] {0, 5, 10, 15}, true);
        Interval interval = ti.getMiddleInterval();
        assertEquals(interval, new Interval(5, 10, true));
    }
}
