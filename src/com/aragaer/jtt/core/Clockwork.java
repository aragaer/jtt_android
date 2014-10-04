package com.aragaer.jtt.core;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Clockwork implements TickCallback {
	private static final String TAG = "JTT CLOCKWORK";
	public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";
	private static final Intent TickAction = new Intent(ACTION_JTT_TICK);

	public static class TimeChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_TIME_CHANGED)
					|| action.equals(Intent.ACTION_DATE_CHANGED))
				try {
					schedule(context);
				} catch (IllegalStateException e) {
					Log.i(TAG, "Time change while service is not running, ignore");
				}
		}
	};

	public static void schedule(final Context context) {
		TickService.setCallback(new Clockwork());
		ThreeIntervals transitions = TransitionProvider.getSurroundingTransitions(context);
		TickService.start(context, transitions.getCurrentStart(), transitions.getCurrentEnd(), Hour.INTERVAL_TICKS);
	}

	public static void unschedule(final Context context) {
		TickService.stop(context);
	}

	public void onTick(Context context) {
		ThreeIntervals transitions = TransitionProvider.getSurroundingTransitions(context);
		Hour hour = Hour.fromTransitions(transitions, System.currentTimeMillis(), null);

		TickAction.putExtra("hour", hour.num).putExtra("jtt", hour.wrapped);
		context.sendStickyBroadcast(TickAction);
	}
}
