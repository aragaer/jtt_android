package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;


public class AndroidClock implements Clock {
    private final Context context;
    private final AndroidClockwork clockwork;

    public AndroidClock(Context context) {
        this.context = context;
        clockwork = new AndroidClockwork(context);
        clockwork.attachTo(clockwork);
    }

    public void adjust() {
        clockwork.schedule();
    }
}
