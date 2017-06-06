// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.android;

import org.junit.*;
import org.junit.runner.RunWith;

import android.content.*;
import android.content.pm.*;
import android.support.test.runner.AndroidJUnit4;

import com.aragaer.jtt.JttService;

import static org.junit.Assert.*;

import static android.support.test.InstrumentationRegistry.getTargetContext;


@RunWith(AndroidJUnit4.class)
public class TimeChangeReceiverTest {

    private Context context;
    private MyReceiver receiver;

    @Before public void setUp() {
	context = getTargetContext();
	receiver = new MyReceiver();
	context.registerReceiver(receiver, new IntentFilter(AndroidTicker.ACTION_JTT_TICK));
    }

    @Test public void testListensForTimeChange() {
	Intent intent = new Intent(Intent.ACTION_TIME_CHANGED);
	PackageManager pm = context.getPackageManager();
	for (ResolveInfo ri : pm.queryBroadcastReceivers(intent, 0)) {
	    ActivityInfo ai = ri.activityInfo;
	    if (ai.name.equals("com.aragaer.jtt.android.TimeChangeReceiver"))
		return;
	}
	fail("Not listening for time change event");
    }

    @Test public void testListensForDateChange() {
	Intent intent = new Intent(Intent.ACTION_DATE_CHANGED);
	PackageManager pm = context.getPackageManager();
	for (ResolveInfo ri : pm.queryBroadcastReceivers(intent, 0)) {
	    ActivityInfo ai = ri.activityInfo;
	    if (ai.name.equals("com.aragaer.jtt.android.TimeChangeReceiver"))
		return;
	}
	fail("Not listening for date change event");
    }

    @Test public void testTriggersTick() throws Exception {
	new TimeChangeReceiver().onReceive(context, new Intent(Intent.ACTION_DATE_CHANGED));
	synchronized (receiver) {
	    receiver.wait();
	}
	assertEquals("Actually got correct event",
		     AndroidTicker.ACTION_JTT_TICK, receiver.triggered.getAction());
    }

    @After public void tearDown() {
	context.unregisterReceiver(receiver);
    }

    private class MyReceiver extends BroadcastReceiver {
	Intent triggered;

	@Override public void onReceive(Context context, Intent intent) {
	    triggered = intent;
	    synchronized (this) {
		this.notify();
	    }
	}
    }
}
