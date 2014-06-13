package com.aragaer.jtt.core;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Clockwork extends IntentService {
	private static final String TAG = "JTT CLOCKWORK";
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
					Log.i(TAG,
							"Time change while service is not running, ignore");
				}
		}
	};

	public static void schedule(final Context context) {
		final AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		FourTransitions transitions = Calculator.getSurroundingTransitions(
				context, System.currentTimeMillis());

		final long tickFrequency = (transitions.currentEnd - transitions.currentStart) / (Hour.HOURS * Hour.HOUR_PARTS);

		final Intent TickActionInternal = new Intent(context, Clockwork.class)
				.putExtra("transitions", transitions);

		am.setRepeating(AlarmManager.RTC, transitions.currentStart,
				tickFrequency, PendingIntent.getService(context, 0,
						TickActionInternal, INTENT_FLAGS));
	}

	public static void unschedule(final Context context) {
		final AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.cancel(PendingIntent.getService(context, 0, new Intent(context,
				Clockwork.class), 0));
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final long now = System.currentTimeMillis();
		FourTransitions transitions = (FourTransitions) intent
				.getParcelableExtra("transitions");
		Hour.fromTransitions(transitions, now, hour);

		if (transitions.isInCurrentInterval(now)) {
			TickAction.putExtra("hour", hour.num).putExtra("jtt", hour.wrapped)
					.putExtra("transitions", transitions);
			sendStickyBroadcast(TickAction);
		} else
			try {
				schedule(this);
			} catch (IllegalStateException e) {
				Log.i(TAG,
						"Transition passed while service is not running, ignore");
			}

		stopSelf();
	}
}
