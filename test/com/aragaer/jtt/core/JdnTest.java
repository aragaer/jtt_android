// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import static org.junit.Assert.*;

import org.junit.Test;


public class JdnTest {

    @Test public void testHaveToTestConstructor() {
        Jdn jdn = new Jdn(); // For now this serves only to fix coverage report
    }

    @Test public void testTimestampToJdn() {
        assertEquals(Jdn.fromTimestamp(0), 2440587);
        assertEquals(Jdn.fromTimestamp(12*60*60*1000-1), 2440587);
        assertEquals(Jdn.fromTimestamp(12*60*60*1000), 2440588);
        assertEquals(Jdn.fromTimestamp(1486502734000L), 2457792);
    }

    @Test public void testJdnToTimestamp() {
        assertEquals(Jdn.toTimestamp(2440588), 43200000);
        assertEquals(Jdn.toTimestamp(2457792L), 1486468800000L);
    }
}
