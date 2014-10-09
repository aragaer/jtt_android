package com.aragaer.jtt.core;

import android.content.Context;
import android.content.Intent;

public class Clockwork implements TickCallback {
	private static final String TAG = "JTT CLOCKWORK";
	public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";
	private static final Intent TickAction = new Intent(ACTION_JTT_TICK);
	private final Context context;

	public Clockwork(Context context) {
		this.context = context;
	}

	public void schedule() {
		TickService.setCallback(this);
		ThreeIntervals transitions = TransitionProvider.getSurroundingTransitions(context);
		TickService.start(context, transitions.getCurrentStart(), transitions.getCurrentEnd(), Hour.INTERVAL_TICKS);
	}

	public void unschedule() {
		TickService.stop(context);
	}

	public void onTick() {
		ThreeIntervals transitions = TransitionProvider.getSurroundingTransitions(context);
		Hour hour = Hour.fromTransitions(transitions, System.currentTimeMillis(), null);

		TickAction.putExtra("hour", hour.num).putExtra("jtt", hour.wrapped);
		context.sendStickyBroadcast(TickAction);
	}
}
