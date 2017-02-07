// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.android;

import com.aragaer.jtt.core.*;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class Ticker extends IntentService {

    public Ticker() {
	super("CLOCKWORK");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        IntervalCalculator calculator = SscAdapter.getInstance();
        IntervalProvider provider = SscCalculator.getInstance(calculator);
	long now = System.currentTimeMillis();
	ThreeIntervals intervals = provider.getIntervalsForTimestamp(now);

	if (intervals.surrounds(now)) {
	    Hour hour = Hour.fromInterval(intervals.getMiddleInterval(), now);
	    Intent TickAction = new Intent(AndroidTicker.ACTION_JTT_TICK)
		.putExtra("intervals", intervals)
		.putExtra("hour", hour.num)
		.putExtra("jtt", hour.wrapped);
	    sendStickyBroadcast(TickAction);
	} else {
	    Clockwork clockwork = new Clockwork(provider);
	    try {
		new AndroidTicker(this, clockwork).start();
	    } catch (IllegalStateException e) {
		Log.i("JTT CLOCKWORK", "Transition passed while service is not running, ignore");
	    }
	}

	stopSelf();
    }
}
