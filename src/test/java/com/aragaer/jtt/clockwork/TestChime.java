package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4


public class TestChime extends Chime {
    private int ticks;

    public TestChime() {
        super(null);
    }

    public int getLastTick() {
        return ticks;
    }

    @Override
    public void ding(int ticks) {
        this.ticks = ticks;
    }
}
