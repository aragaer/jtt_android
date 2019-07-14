// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClockworkTest {

    @Test public void testTimer() {
        Clockwork clockwork = new Clockwork(new TestIntervalProvider());
        clockwork.setTime(1000);
        assertEquals(clockwork.start, 1000-240);
        assertEquals(clockwork.repeat, 2);
    }

    private static class TestIntervalProvider implements IntervalProvider {
        @Override public ThreeIntervals getIntervalsForTimestamp(long time) {
            int ticklen = 2;
            int intervalLen = Hour.TICKS_PER_INTERVAL * ticklen;
            return new ThreeIntervals(new long[] {time - intervalLen*3/2, time - intervalLen/2,
                                                  time + intervalLen/2, time + intervalLen*3/2}, true);
        }
    }
}
