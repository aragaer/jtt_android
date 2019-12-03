// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.mechanics;

import android.content.Context;
import android.content.Intent;

import com.aragaer.jtt.core.*;


public class AndroidAnnouncer implements Announcer {

    private final Context _context;
    private final IntervalProvider _intervalProvider;

    public AndroidAnnouncer(Context context, IntervalProvider intervalProvider) {
        _context = context;
        _intervalProvider = intervalProvider;
    }

    @Override public void announce(long timestamp) {
        ThreeIntervals intervals = _intervalProvider.getIntervalsForTimestamp(timestamp);
        Hour hour = Hour.fromInterval(intervals.getMiddleInterval(), timestamp);
        Intent intent = new Intent(AndroidTicker.ACTION_JTT_TICK)
                .putExtra("intervals", intervals)
                .putExtra("hour", hour.num)
                .putExtra("jtt", hour.wrapped);
        _context.sendStickyBroadcast(intent);
    }
}
