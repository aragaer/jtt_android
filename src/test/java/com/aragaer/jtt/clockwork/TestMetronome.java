package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4


public class TestMetronome implements Metronome {

    private Cogs cogs;
    public long start;
    public long tickLength;

    public void attachTo(Cogs cogs) {
        this.cogs = cogs;
    }

    public void start(long start, long tickLength) {
        this.start = start;
        this.tickLength = tickLength;
    }

    public void stop() {}

    public void tick(int times) {
        cogs.rotate(times);
    }
}
