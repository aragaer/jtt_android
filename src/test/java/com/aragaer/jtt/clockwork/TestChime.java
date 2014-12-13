package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4


public class TestChime implements Chime {
    private int ticks;

    public int getLastTick() {
        return ticks;
    }

    public void ding(int ticks) {
        this.ticks = ticks;
    }
}
