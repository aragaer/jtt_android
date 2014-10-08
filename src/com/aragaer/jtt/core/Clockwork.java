package com.aragaer.jtt.core;

import android.content.Context;
import android.content.Intent;

public class Clockwork implements TickCallback {
	private static final String TAG = "JTT CLOCKWORK";
	public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";
	private static final Intent TickAction = new Intent(ACTION_JTT_TICK);

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
