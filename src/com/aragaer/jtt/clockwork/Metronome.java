package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

public interface Metronome {
    public void attachTo(Clock clock);
    public void start(long start, long tickLength);
    public void setStopTime(long stopAt);
    public void stop();
}
