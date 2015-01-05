package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;
import android.content.Intent;

import com.aragaer.jtt.clockwork.TickClient;


public class TickBroadcast implements TickClient {
    public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";
    public static final String EXTRA_JTT = "jtt";

    private final Context context;

    public TickBroadcast(Context context) {
        this.context = context;
    }

    public void tickChanged(int tick) {
        context.sendStickyBroadcast(new Intent(ACTION_JTT_TICK).putExtra(EXTRA_JTT, tick));
    }
}
