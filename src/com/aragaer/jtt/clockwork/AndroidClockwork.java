package com.aragaer.jtt.clockwork;

import android.content.Context;
import android.content.Intent;

import com.aragaer.jtt.core.Hour;
import com.aragaer.jtt.core.ThreeIntervals;
import com.aragaer.jtt.core.TickCallback;
import com.aragaer.jtt.core.TransitionProvider;


public class AndroidClockwork implements TickCallback {
	private static final String TAG = "JTT CLOCKWORK";
	public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";
	private static final Intent TickAction = new Intent(ACTION_JTT_TICK);
	private final Context context;

	public AndroidClockwork(Context context) {
		this.context = context;
	}

	public void onTick() {
		ThreeIntervals transitions = TransitionProvider.getSurroundingTransitions(context);
		Hour hour = Hour.fromTransitions(transitions, System.currentTimeMillis(), null);

		TickAction.putExtra("hour", hour.num).putExtra("jtt", hour.wrapped);
		context.sendStickyBroadcast(TickAction);
	}
}
