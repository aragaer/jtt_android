// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.android;

import com.aragaer.jtt.core.Hour;
import com.aragaer.jtt.core.ThreeIntervals;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class Ticker extends IntentService {

    public Ticker() {
	super("CLOCKWORK");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
	ThreeIntervals intervals = (ThreeIntervals) intent.getSerializableExtra("intervals");
	long now = System.currentTimeMillis();

	if (intervals.surrounds(now)) {
	    Hour hour = Hour.fromInterval(intervals.getMiddleInterval(), now, null);
	    Intent TickAction = new Intent(AndroidTicker.ACTION_JTT_TICK)
		.putExtra("intervals", intervals)
		.putExtra("hour", hour.num)
		.putExtra("jtt", hour.wrapped);
	    sendStickyBroadcast(TickAction);
	} else
	    try {
		new AndroidTicker(this).start();
	    } catch (IllegalStateException e) {
		Log.i("JTT CLOCKWORK", "Transition passed while service is not running, ignore");
	    }

	stopSelf();
    }
}
