package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.core.TickCallback;


public class ClockworkTickCallback implements TickCallback {

    private final Clockwork clockwork;
    long lastTick;
    final long length;

    public ClockworkTickCallback(Clockwork clockwork, long intervalStart, long tickLength) {
        this.clockwork = clockwork;
        lastTick = intervalStart;
        length = tickLength;
    }

    public void onTick() {
        long now = System.currentTimeMillis();
        long passed = now - lastTick;
        long ticks = passed / length;
        lastTick += ticks * length;
        clockwork.tick((int) ticks);
    }
}
