// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import com.aragaer.jtt.android.SunriseSunsetDataProvider;
import android.content.Context;


public class Clockwork {
    public long start, repeat;

    private final Context _context;

    public Clockwork(Context context) {
	_context = context;
    }

    public void setTime(long time) {
	ThreeIntervals intervals = SunriseSunsetDataProvider.getSurroundingTransitions(_context, time);
	Interval currentInterval = intervals.getMiddleInterval();
	start = currentInterval.start;
	repeat = Math.round(currentInterval.getLength()/Hour.TICKS_PER_INTERVAL);
    }
}
