package com.aragaer.jtt.clockwork.android;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.clockwork.TickCounter;


public class ClockTickCallback {

    private final TickCounter counter;
    private long lastTick;
    private final long length;

    public ClockTickCallback(TickCounter counter, long intervalStart, long tickLength) {
        this.counter = counter;
        lastTick = intervalStart;
        length = tickLength;
    }

    public void onTick() {
        long now = System.currentTimeMillis();
        long passed = now - lastTick;
        long ticks = passed / length;
        counter.set((int) ticks);
    }
}
