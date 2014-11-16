package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4


public interface ClockEvent {
    public void trigger(int ticks);
    public int getGranularity();
}
