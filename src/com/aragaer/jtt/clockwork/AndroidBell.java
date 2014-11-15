package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;
import android.content.Intent;

import com.aragaer.jtt.core.Hour;
import com.aragaer.jtt.core.ThreeIntervals;
import com.aragaer.jtt.core.TransitionProvider;


public class AndroidBell implements Bell {
    public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";
    private static final Intent TickAction = new Intent(ACTION_JTT_TICK);
    private final Context context;

    public AndroidBell(Context context) {
        this.context = context;
    }

    public void ring(int ticks) {
        TickAction.putExtra("jtt", ticks);
        context.sendStickyBroadcast(TickAction);
    }
}
