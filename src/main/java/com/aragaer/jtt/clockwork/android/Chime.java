package com.aragaer.jtt.clockwork.android;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;
import android.content.Intent;


public class Chime implements com.aragaer.jtt.clockwork.Chime {
    public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";
    public static final String EXTRA_JTT = "jtt";

    private final Context context;

    public Chime(Context context) {
        this.context = context;
    }

    public void ding(int tick) {
        context.sendStickyBroadcast(new Intent(ACTION_JTT_TICK).putExtra(EXTRA_JTT, tick));
    }
}
