package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4


public class ClockworkTickCallback implements TickCallback {

    private final Clock clock;
    private long lastTick;
    private final long length;
    private final long intervalEnd;

    public ClockworkTickCallback(Clock clock, long intervalStart, long tickLength, long intervalEnd) {
        this.clock = clock;
        lastTick = intervalStart;
        length = tickLength;
        this.intervalEnd = intervalEnd;
    }

    public ClockworkTickCallback(Clock clock, long intervalStart, long tickLength) {
        this(clock, intervalStart, tickLength, -1);
    }

    public void onTick() {
        long now = System.currentTimeMillis();
        if (intervalEnd != -1 && now > intervalEnd)
            now = intervalEnd;
        long passed = now - lastTick;
        long ticks = passed / length;
        lastTick += ticks * length;
        clock.tick((int) ticks);
    }
}
