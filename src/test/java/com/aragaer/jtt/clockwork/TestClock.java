package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.astronomy.DayInterval;


public class TestClock extends Clock {

    public DayInterval currentInterval;

    public TestClock(Chime chime, Metronome metronome) {
        super(chime, metronome);
    }

    @Override
    public void intervalChanged(DayInterval interval) {
        currentInterval = interval;
        super.intervalChanged(interval);
    }
}
