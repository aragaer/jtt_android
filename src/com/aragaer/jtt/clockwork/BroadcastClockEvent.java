package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;
import android.content.Intent;


public class BroadcastClockEvent implements ClockEvent {
    private final Intent intent;
    private final Context context;
    private final int granularity;

    public BroadcastClockEvent(Context context, String action, int granularity) {
        this.context = context;
        this.intent = new Intent(action);
        this.granularity = granularity;
    }

    public int getGranularity() {
        return granularity;
    }

    public void trigger(int ticks) {
        intent.putExtra("jtt", ticks);
        context.sendStickyBroadcast(intent);
    }
}
