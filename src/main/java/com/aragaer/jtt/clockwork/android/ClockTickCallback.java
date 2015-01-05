package com.aragaer.jtt.clockwork.android;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.clockwork.TickCounter;


public class ClockTickCallback {

    private final TickCounter cogs;
    private long lastTick;
    private final long length;

    public ClockTickCallback(TickCounter cogs, long intervalStart, long tickLength) {
        this.cogs = cogs;
        lastTick = intervalStart;
        length = tickLength;
    }

    public void onTick() {
        long now = System.currentTimeMillis();
        long passed = now - lastTick;
        long ticks = passed / length;
        lastTick += ticks * length;
        cogs.rotate((int) ticks);
    }
}
