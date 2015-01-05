package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4


public class TestMetronome implements TickProvider {

    public long start;
    public long tickLength;
    private TickCounter counter;

    public void attachTo(TickCounter counter) {
        this.counter = counter;
    }

    public void start(long start, long tickLength) {
        this.start = start;
        this.tickLength = tickLength;
    }

    public void stop() {}

    public void tick(int ticks) {
        counter.rotate(ticks);
    }
}
