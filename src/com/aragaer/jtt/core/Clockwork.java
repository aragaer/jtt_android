package com.aragaer.jtt.core;

import java.lang.IllegalStateException;
import java.util.Calendar;
import java.util.TimeZone;

import com.aragaer.jtt.JTT;
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
	private SunriseSunsetCalculator calculator;
	private boolean is_ticking;

	public static final String ENABLE_ACTION = "com.aragaer.jtt.action.ENABLE";
	private static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK_INTERNAL";
	private final Intent TickActionInternal = new Intent(ACTION_JTT_TICK),
		TickAction = new Intent("com.aragaer.jtt.ACTION_JTT_TICK");
	private final static int INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT;

	public Clockwork() {
		super("CLOCKWORK");
	}

	private final static int ticks = 6;
	private final static int subs = 100;
	private final static double total = ticks * subs;

	public void schedule() {
		final AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		if (calculator == null) {
			final float l[] = Settings.getLocation(this);
			calculator = new SunriseSunsetCalculator(new Location(l[0], l[1]), TimeZone.getDefault());
		}

		final long tr[] = new long[2];
		final boolean is_day = getSurroundingTransitions(calculator, tr);

		final long freq = Math.round((tr[1] - tr[0])/total);

		TickActionInternal.putExtra("tr", tr);
		TickActionInternal.putExtra("day", is_day);

		/* Tell alarm manager to start ticking at tr[0], it will automatically calculate the next tick time */
		am.setRepeating(AlarmManager.RTC, tr[0], freq, PendingIntent.getService(this, 0, TickActionInternal, INTENT_FLAGS));
	}

	public void unschedule() {
		final AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.cancel(PendingIntent.getService(this, 0, TickActionInternal, 0));
	}

	public synchronized void enable() {
		if (is_ticking)
			throw new IllegalStateException("Clockwork is already enabled");
		is_ticking = true;

		schedule();
	}

	public synchronized void disable() {
		if (!is_ticking)
			throw new IllegalStateException("Clockwork is already disabled");
		is_ticking = false;

		unschedule();
	}

	protected void onHandleIntent(Intent intent) {
		if (intent == null) {
			Log.e("CLOCKWORK", "Intent is null!");
			return;
		}

		final String action = intent.getAction();
		if (action == null) {
			Log.e("CLOCKWORK", "Action is null!");
			return;
		}

		if (action.equals(ENABLE_ACTION)) {
			enable();
		} else if (action.equals(ACTION_JTT_TICK)) {
			final long tr[] = intent.getLongArrayExtra("tr");
			final boolean is_day = intent.getBooleanExtra("day", false);
			final long now = System.currentTimeMillis();
			int jtt[] = timestamps2jtt(tr, is_day, now);
			Log.d("CLOCKWORK", "Tick "+jtt[0]+":"+jtt[1]);

			if (now >= tr[0] && now < tr[1]) {
				TickAction.putExtra("hour", jtt[0]);
				TickAction.putExtra("fraction", jtt[1]);

				sendStickyBroadcast(TickAction);
			}
		}
	}

	private static int[] timestamps2jtt(final long tr[], final boolean is_day, final long now) {
		final int out[] = new int[2];
		final double passed = (1. * now - tr[0]) / (tr[1] - tr[0]);
		out[1] = (int) (total * passed);
		out[0] = out[1] / subs + (is_day ? 6 : 0);
		out[1] %= subs;
		return out;
	}

	/* Put surrounding transitions into tr, return true if it is day now */
	private static boolean getSurroundingTransitions(final SunriseSunsetCalculator calculator, final long tr[]) {
		final long now = System.currentTimeMillis();
		Calendar date = Calendar.getInstance();
		tr[0] = calculator.getOfficialSunriseCalendarForDate(date).getTimeInMillis();
		tr[1] = calculator.getOfficialSunsetCalendarForDate(date).getTimeInMillis();
		boolean is_day = true;

		// if tr2 is before now
		while (now >= tr[1]) {
			tr[0] = tr[1];
			if (is_day) {
				date.add(Calendar.DATE, 1);
				tr[1] = calculator.getOfficialSunriseCalendarForDate(date).getTimeInMillis();
			} else
				tr[1] = calculator.getOfficialSunsetCalendarForDate(date).getTimeInMillis();
			is_day = !is_day;
		}

		// (else) if tr1 is after now
		while (now < tr[0]) {
			tr[1] = tr[0];
			if (is_day) {
				date.add(Calendar.DATE, -1);
				tr[0] = calculator.getOfficialSunsetCalendarForDate(date).getTimeInMillis();
			} else
				tr[0] = calculator.getOfficialSunriseCalendarForDate(date).getTimeInMillis();
			is_day = !is_day;
		}

		return is_day;
	}
}
