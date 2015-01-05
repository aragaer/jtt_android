package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public abstract class ChimeListener extends BroadcastReceiver {

    public void register(Context context) {
        context.registerReceiver(this, new IntentFilter(TickBroadcast.ACTION_JTT_TICK));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        onChime(context, intent.getIntExtra(TickBroadcast.EXTRA_JTT, 0));
    }

    public abstract void onChime(Context context, int ticks);
}
