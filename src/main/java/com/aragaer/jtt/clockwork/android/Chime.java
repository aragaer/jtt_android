package com.aragaer.jtt.clockwork.android;
// vim: et ts=4 sts=4 sw=4

import javax.inject.Inject;

import android.content.Context;
import android.content.Intent;


public class Chime implements com.aragaer.jtt.clockwork.Chime {
    public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";
    public static final String EXTRA_JTT = "jtt";

    @Inject Context context;

    public void ding(int tick) {
        context.sendStickyBroadcast(new Intent(ACTION_JTT_TICK).putExtra(EXTRA_JTT, tick));
    }
}
