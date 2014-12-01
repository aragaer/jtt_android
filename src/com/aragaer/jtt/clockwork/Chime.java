package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;
import android.content.Intent;


public class Chime {

    private final Context context;

    public Chime(Context context) {
        this.context = context;
    }

    public void ding(int tick) {
        context.sendStickyBroadcast(
                new Intent(AndroidClock.ACTION_JTT_TICK)
                    .putExtra(AndroidClock.EXTRA_JTT, tick));
    }
}
