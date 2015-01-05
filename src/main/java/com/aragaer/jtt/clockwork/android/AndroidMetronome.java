package com.aragaer.jtt.clockwork.android;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;

import com.aragaer.jtt.clockwork.Cogs;
import com.aragaer.jtt.clockwork.Metronome;


public class AndroidMetronome implements Metronome {
    private final Context context;
    private Cogs cogs;

    public AndroidMetronome(Context context) {
        this.context = context;
    }

    public void attachTo(Cogs newCogs) {
        cogs = newCogs;
    }

    public void start(long start, long tickLength) {
        TickService.setCallback(new ClockTickCallback(cogs, start, tickLength));
        TickService.start(context, start, tickLength);
    }

    public void stop() {
        TickService.stop(context);
    }
}
