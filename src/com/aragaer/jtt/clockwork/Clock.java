package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;

import com.aragaer.jtt.core.Clockwork;


public class Clock {
    private final Context context;

    public Clock(Context context) {
        this.context = context;
    }

    public void adjust() {
        Clockwork.schedule(this.context);
    }
}
