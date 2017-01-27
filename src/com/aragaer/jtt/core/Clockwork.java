// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import com.aragaer.jtt.android.SunriseSunsetDataProvider;
import android.content.Context;
import java.io.Serializable;


public class Clockwork {
    public final long start, repeat;
    public Serializable data;

    public Clockwork(Context context, long now) {
	ThreeIntervals intervals = SunriseSunsetDataProvider.getSurroundingTransitions(context, now);
	Interval currentInterval = intervals.getMiddleInterval();
	start = currentInterval.start;
	repeat = Math.round(currentInterval.getLength()/Hour.TICKS_PER_INTERVAL);
	data = intervals;
    }
}
