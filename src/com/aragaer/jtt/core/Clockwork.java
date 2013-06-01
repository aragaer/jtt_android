package com.aragaer.jtt.core;

import java.lang.IllegalStateException;
import java.util.Calendar;
import java.util.TimeZone;

import com.aragaer.jtt.JTTHour;
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

	public static final int ACTION_ENABLE = 0,
		ACTION_DISABLE = 1,
		ACTION_TRIGGER = 2,
		ACTION_MOVE = 3;
	public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";
	private static final int ACTION_JTT_TICK2 = 42;
	private final static Intent TickAction = new Intent(ACTION_JTT_TICK);
	private final static int INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT;

	public Clockwork() {
		super("CLOCKWORK");
	}

	public static class TimeChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(Intent.ACTION_TIME_CHANGED)
					|| action.equals(Intent.ACTION_DATE_CHANGED))
				context.startService(new Intent(context, Clockwork.class).putExtra("action", ACTION_TRIGGER));
		}
	};

	private final static int ticks = JTTHour.ticks;
	private final static int subs = JTTHour.subs;
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

		final Intent TickActionInternal = new Intent(this, Clockwork.class)
				.putExtra("action", ACTION_JTT_TICK2)
				.putExtra("tr", tr)
				.putExtra("day", is_day);

		/* Tell alarm manager to start ticking at tr[0], it will automatically calculate the next tick time */
		am.setRepeating(AlarmManager.RTC, tr[0], freq, PendingIntent.getService(this, 0, TickActionInternal, INTENT_FLAGS));
	}

	public void unschedule() {
		final AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.cancel(PendingIntent.getService(this, 0, new Intent(this, Clockwork.class), 0));
	}

	protected void onHandleIntent(Intent intent) {
		switch (intent.getIntExtra("action", 0)) {
		case ACTION_MOVE:
			if (calculator != null) {
				final float l[] = intent.getFloatArrayExtra("location");
				calculator = new SunriseSunsetCalculator(new Location(l[0], l[1]), TimeZone.getDefault());
			}
			/* fall-through */
		case ACTION_ENABLE:
		case ACTION_TRIGGER:
			schedule();
			break;
		case ACTION_DISABLE:
			unschedule();
			break;
		case ACTION_JTT_TICK2:
			final long tr[] = intent.getLongArrayExtra("tr");
			final boolean is_day = intent.getBooleanExtra("day", false);
			final long now = System.currentTimeMillis();
			int jtt[] = timestamps2jtt(tr, is_day, now);
			Log.d("CLOCKWORK", "Tick "+jtt[0]+":"+jtt[1]);

			if (now >= tr[0] && now < tr[1]) {
				TickAction.putExtra("tr", tr)
						.putExtra("day", is_day)
						.putExtra("hour", jtt[0])
						.putExtra("fraction", jtt[1]);

				sendStickyBroadcast(TickAction);
			} else {
				schedule();
			}
			break;
		default:
			break;
		}

		stopSelf();
	}

	private static int[] timestamps2jtt(final long tr[], final boolean is_day, final long now) {
		final int out[] = new int[2];
		final double passed = (1. * now - tr[0]) / (tr[1] - tr[0]);
		out[1] = (int) (total * passed);
		out[0] = out[1] / subs + (is_day ? ticks : 0);
		out[1] %= subs;
		return out;
	}

	/* Put surrounding transitions into tr, return true if it is day now */
	private static boolean getSurroundingTransitions(final SunriseSunsetCalculator calculator, final long tr[]) {
		final long now = System.currentTimeMillis();
		Calendar date = Calendar.getInstance();
		tr[0] = calculator.getOfficialSunriseForDate(date);
		tr[1] = calculator.getOfficialSunsetForDate(date);
		boolean is_day = true;

		// if tr2 is before now
		while (now >= tr[1]) {
			tr[0] = tr[1];
			if (is_day) {
				date.add(Calendar.DATE, 1);
				tr[1] = calculator.getOfficialSunriseForDate(date);
			} else
				tr[1] = calculator.getOfficialSunsetForDate(date);
			is_day = !is_day;
		}

		// (else) if tr1 is after now
		while (now < tr[0]) {
			tr[1] = tr[0];
			if (is_day) {
				date.add(Calendar.DATE, -1);
				tr[0] = calculator.getOfficialSunsetForDate(date);
			} else
				tr[0] = calculator.getOfficialSunriseForDate(date);
			is_day = !is_day;
		}

		return is_day;
	}
}
