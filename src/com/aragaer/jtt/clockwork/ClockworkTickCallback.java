package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4


public class ClockworkTickCallback implements TickCallback {

    private final Clockwork clockwork;
    private long lastTick;
    private final long length;
    private final long intervalEnd;

    public ClockworkTickCallback(Clockwork clockwork, long intervalStart, long tickLength, long intervalEnd) {
        this.clockwork = clockwork;
        lastTick = intervalStart;
        length = tickLength;
        this.intervalEnd = intervalEnd;
    }

    public ClockworkTickCallback(Clockwork clockwork, long intervalStart, long tickLength) {
        this(clockwork, intervalStart, tickLength, -1);
    }

    public void onTick() {
        long now = System.currentTimeMillis();
        if (intervalEnd != -1 && now > intervalEnd)
            now = intervalEnd;
        long passed = now - lastTick;
        long ticks = passed / length;
        lastTick += ticks * length;
        clockwork.tick((int) ticks);
    }
}
