package com.aragaer.jtt.core;

import java.util.Calendar;
import java.util.TimeZone;

import com.aragaer.jtt.Settings;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

	private final static double total = Hour.HOURS * Hour.HOUR_PARTS;

	public static void schedule(final Context context) {
		final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final long tr[] = new long[4];
		final boolean is_day = Calculator.getSurroundingTransitions(context, System.currentTimeMillis(), tr);

		final long freq = Math.round((tr[2] - tr[1])/total);

		final Intent TickActionInternal = new Intent(context, Clockwork.class)
				.putExtra("tr", tr)
				.putExtra("day", is_day);

		/* Tell alarm manager to start ticking at tr[1], it will automatically calculate the next tick time */
		am.setRepeating(AlarmManager.RTC, tr[1], freq, PendingIntent.getService(context, 0, TickActionInternal, INTENT_FLAGS));
	}

	public static void unschedule(final Context context) {
		final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(PendingIntent.getService(context, 0, new Intent(context, Clockwork.class), 0));
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final long tr[] = intent.getLongArrayExtra("tr");
		final boolean is_day = intent.getBooleanExtra("day", false);
		final long now = System.currentTimeMillis();
		Hour.fromTimestamps(tr, is_day, now, hour);

		if (now >= tr[1] && now < tr[2]) {
			TickAction.putExtra("tr", tr)
					.putExtra("day", is_day)
					.putExtra("hour", hour.num)
					.putExtra("jtt", hour.wrapped);
			sendStickyBroadcast(TickAction);
		} else
			schedule(this);

		stopSelf();
	}
}
