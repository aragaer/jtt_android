package com.aragaer.jtt;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/* the thing that actually does change with time */
class Clockwork extends Handler {
	public static final int MSG_SYNC = 9;

	private WeakReference<Context> ctx;
	private final WeakReference<JTT> calc;

	private long sync = 0;
	protected ArrayList<Long> transitions = new ArrayList<Long>();
	private long start_day, end_day;

	private int hour, sub;

	private final BroadcastReceiver on = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			wake_up();
		}
	};
	private final BroadcastReceiver off = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			go_sleep();
		}
	};

	public Clockwork(JTT c) {
		calc = new WeakReference<JTT>(c);
	}

	void set_context(Context context) {
		ctx = new WeakReference<Context>(context);
		IntentFilter wake = new IntentFilter(Intent.ACTION_SCREEN_ON);
		wake.addAction(Intent.ACTION_TIME_CHANGED);
		wake.addAction(Intent.ACTION_DATE_CHANGED);
		context.registerReceiver(on, wake);
		context.registerReceiver(off, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_SYNC:
			wake_up();
			break;
		default:
			super.handleMessage(msg);
			break;
		}
	}

	private final static int ticks = 6;
	private final static int subs = 100;
	private final static double total = ticks * subs;
	/* This function assumes that we have just woke up
	 * Do not attempt to short-cut any calculations based on previous runs
	 */
	private void wake_up() {
		final JTT calculator = calc.get();
		long now, start, end;
		int isDay;

		/* do not want more than one message being in the system */
		removeMessages(MSG_SYNC);
		while (true) { // we are likely to pass this loop only once
			long[] t = null;
			int len = transitions.size();
			now = System.currentTimeMillis();
			int pos = Collections.binarySearch(transitions, now);
			/*
			 * Possible results:
			 * -len - 1:        overrun
			 * -len - 2 to -2:  OK (tr[-pos - 2] < now < tr[-pos - 1])
			 * -1:              underrun
			 * 0 to len - 2:    OK (tr[pos] == now < tr[pos + 1])
			 * len - 1:         overrun
			 *
			 * if len == 0, the only result is -1
			 * if len == 1, there's no OK results
			 */
			if (pos == -1) // right before first element
				t = calculator.computeTr(start_day--);
			if (pos == -len - 1             // after the last element
					|| pos == len - 1)      // equal to the last element
				t = calculator.computeTr(end_day++);
			if (t != null) {
				for (long l : t)
					transitions.add(l);
				// all this took some time, we should resync
				continue;
			}

			if (pos < 0)
				pos = -pos - 2;

			start = transitions.get(pos);
			end = transitions.get(pos + 1);
			/* cheat - sunrises always have even positions */
			isDay = (pos + 1) % 2;
			break;
		}

		/* we've got start and end */
		long offset = now - start;
		double sublen = ((double) (end - start))/total;
		int exp_total = (int) (offset/sublen);
		int exp_tick = exp_total / subs;
		int exp_sub = exp_total % subs;
		long next_sub = start + Math.round(sublen * (exp_total + 1));

		if (now - sync > exp_sub * sublen // sync belongs to previous tick interval
				|| now < sync) {          // time went backwards!
			hour = exp_tick + isDay * 6;
			sub = exp_sub;
		} else if (sub < exp_sub) {
			if (hour % 6 != exp_tick) { // sync should belong to this tick interval
				Log.e("CLOCK_MECHANISM", "current tick is "+(hour % 6)+", expected "+exp_tick);
				hour = exp_tick + isDay * 6;
			}
			sub = exp_sub;
		}
		// broadcast current time

		sync = System.currentTimeMillis();
		/* doesn't matter if next_sub < sync
		 * negative delay is perfectly valid and means that trigger will happen immediately
		 */
		sendEmptyMessageDelayed(MSG_SYNC, next_sub - sync);
	}

	private final void go_sleep() {
		removeMessages(MSG_SYNC);
	}

	final void reset() {
		final JTT calculator = calc.get();
		Log.d("PROVIDER", "Clock is reset");

		go_sleep();
		transitions.clear();
		end_day = JTT.longToJDN(System.currentTimeMillis());
		start_day = end_day - 1;
		for (long l : calculator.computeTr(end_day++))
			transitions.add(l);
		sync = 0;
		wake_up();
	}
}