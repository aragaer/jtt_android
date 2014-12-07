package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;


public class AndroidMetronome implements Metronome {
    private final Context context;
    private Clock clock;

    public AndroidMetronome(Context context) {
        this.context = context;
    }

    public void attachTo(Clock newClock) {
        clock = newClock;
    }

    public void start(long start, long tickLength) {
        TickService.setCallback(new ClockTickCallback(clock, start, tickLength));
        TickService.start(context, start, tickLength);
    }

    public void stop() {
        TickService.stop(context);
    }
}
