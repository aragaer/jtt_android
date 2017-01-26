package com.aragaer.jtt.core;

import android.app.*;
import android.content.*;
import android.util.Log;

public class Clockwork extends IntentService {
    public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";
    private static final Intent TickAction = new Intent(ACTION_JTT_TICK);
    private static final int INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT;
    private final Hour hour = new Hour(0);

    public Clockwork() {
	super("CLOCKWORK");
    }

    public static class TimeChangeReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
	    final String action = intent.getAction();
	    if (action.equals(Intent.ACTION_TIME_CHANGED)
		|| action.equals(Intent.ACTION_DATE_CHANGED))
		try {
		    schedule(context);
		} catch (IllegalStateException e) {
		    Log.i("JTT CLOCKWORK", "Time change while service is not running, ignore");
		}
	}
    };

    private final static double ticksPerInterval = Hour.HOURS * Hour.HOUR_PARTS;

    public static void schedule(final Context context) {
	ThreeIntervals intervals = Calculator.getSurroundingTransitions(context, System.currentTimeMillis());
	long currentIntervalStart = intervals.getTransitions()[1];
	long currentIntervalEnd = intervals.getTransitions()[2];
	long tickLength = Math.round((currentIntervalEnd - currentIntervalStart)/ticksPerInterval);

	Intent TickActionInternal = new Intent(context, Clockwork.class)
	    .putExtra("intervals", intervals);

	AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	am.setRepeating(AlarmManager.RTC, currentIntervalStart, tickLength,
			PendingIntent.getService(context, 0, TickActionInternal, INTENT_FLAGS));
    }

    public static void unschedule(final Context context) {
	AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	am.cancel(PendingIntent.getService(context, 0, new Intent(context, Clockwork.class), 0));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
	final ThreeIntervals intervals = (ThreeIntervals) intent.getSerializableExtra("intervals");
	final long now = System.currentTimeMillis();
	Hour.fromIntervals(intervals, now, hour);

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
