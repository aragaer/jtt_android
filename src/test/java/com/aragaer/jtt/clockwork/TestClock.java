package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.astronomy.DayInterval;


public class TestClock extends Clock {

    public int ticks;
    public DayInterval currentInterval;
    private final TestChime chime;

    public TestClock() {
        this(new TestChime());
    }

    private TestClock(TestChime chime) {
        super(chime, new TestMetronome());
        this.chime = chime;
    }

    @Override
    public void tick(int ticks) {
        super.tick(ticks);
        this.ticks = chime.getLastTick();
    }

    @Override
    public void setInterval(DayInterval interval) {
        currentInterval = interval;
        super.setInterval(interval);
    }
}
