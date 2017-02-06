// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;


public class Clockwork {
    public long start, repeat;

    private final IntervalProvider _holder;

    public Clockwork(IntervalProvider intervalProvider) {
        _holder = intervalProvider;
    }

    public void setTime(long time) {
        ThreeIntervals intervals = _holder.getIntervalsForTimestamp(time);
        Interval currentInterval = intervals.getMiddleInterval();
        start = currentInterval.start;
        repeat = Math.round(currentInterval.getLength()/Hour.TICKS_PER_INTERVAL);
    }
}
