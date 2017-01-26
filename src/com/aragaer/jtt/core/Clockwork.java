package com.aragaer.jtt.core;

import android.app.*;
import android.content.*;
import android.util.Log;

public class Clockwork extends IntentService {
    public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";
    private static final Intent TickAction = new Intent(ACTION_JTT_TICK);
    private static final int INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT;

    public Clockwork() {
	super("CLOCKWORK");
    }

    public static void schedule(final Context context) {
	long now = System.currentTimeMillis();
	ThreeIntervals intervals = Calculator.getSurroundingTransitions(context, now);
	Interval currentInterval = intervals.getMiddleInterval();
	long tickLength = Math.round(currentInterval.getLength()/Hour.TICKS_PER_INTERVAL);

	Intent TickActionInternal = new Intent(context, Clockwork.class)
	    .putExtra("intervals", intervals);

	AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	am.setRepeating(AlarmManager.RTC, currentInterval.start, tickLength,
			PendingIntent.getService(context, 0, TickActionInternal, INTENT_FLAGS));
    }

    public static void unschedule(final Context context) {
	AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	am.cancel(PendingIntent.getService(context, 0, new Intent(context, Clockwork.class), 0));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
	ThreeIntervals intervals = (ThreeIntervals) intent.getSerializableExtra("intervals");
	long now = System.currentTimeMillis();
	Hour hour = Hour.fromInterval(intervals.getMiddleInterval(), now, null);

	if (intervals.surrounds(now)) {
	    TickAction.putExtra("intervals", intervals)
		.putExtra("hour", hour.num)
		.putExtra("jtt", hour.wrapped);
	    sendStickyBroadcast(TickAction);
	} else
	    try {
		schedule(this);
	    } catch (IllegalStateException e) {
		Log.i("JTT CLOCKWORK", "Transition passed while service is not running, ignore");
	    }

	stopSelf();
    }
}
