package com.aragaer.jtt.clockwork.android;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;

import com.aragaer.jtt.clockwork.TickCounter;
import com.aragaer.jtt.clockwork.TickProvider;


public class AndroidMetronome implements TickProvider {
    private final Context context;
    private TickCounter cogs;

    public AndroidMetronome(Context context) {
        this.context = context;
    }

    public void attachTo(TickCounter newTickCounter) {
        cogs = newTickCounter;
    }

    public void start(long start, long tickLength) {
        TickService.setCallback(new ClockTickCallback(cogs, start, tickLength));
        TickService.start(context, start, tickLength);
    }

    public void stop() {
        TickService.stop(context);
    }
}
