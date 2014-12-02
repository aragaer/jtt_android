package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4


public class TestMetronome implements Metronome {

    private Clockwork clockwork;
    public long start;
    public long tickLength;

    public void attachTo(Clockwork clockwork) {
        this.clockwork = clockwork;
    }

    public void start(long start, long tickLength) {
        this.start = start;
        this.tickLength = tickLength;
    }

    public void stop() {}

    public void setStopTime(long stopAt) {}

    public void tick(int times) {
        clockwork.tick(times);
    }
}
