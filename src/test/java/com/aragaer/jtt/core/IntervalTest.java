// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import static org.junit.Assert.*;

import org.junit.Test;


public class IntervalTest {

    @Test public void testCreate() {
        Interval interval = new Interval(0, 1, true);
        assertEquals(interval.start, 0);
        assertEquals(interval.end, 1);
        assertTrue(interval.is_day);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_thatStartMustBeBeforeEnd() {
        new Interval(5, 0, true);
    }

    @Test public void testCanBeCompared() {
        Interval i1 = new Interval(0, 1, true);
        Interval i2 = new Interval(0, 1, true);
        Interval i3 = new Interval(0, 2, true);
        Interval i4 = new Interval(0, 1, false);
        Interval i5 = new Interval(-1, 1, true);
        assertEquals(i1, i1);
        assertEquals(i1, i2);
        assertNotEquals(i1, null);
        assertNotEquals(i1, this);
        assertNotEquals(i1, i3);
        assertNotEquals(i1, i4);
        assertNotEquals(i1, i5);
        assertEquals(i1.hashCode(), i2.hashCode());
        assertNotEquals(i1.hashCode(), i3.hashCode());
        assertNotEquals(i1.hashCode(), i4.hashCode());
    }

    @Test public void testLength() {
        Interval i = new Interval(0, 1, true);
        assertEquals(i.getLength(), 1);
    }
}
