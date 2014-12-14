package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import javax.inject.Inject;

import com.aragaer.jtt.astronomy.DayInterval;


public class TestClock extends Clock {

    public DayInterval currentInterval;

    @Inject TestClock(Chime chime, Metronome metronome) {
        super(chime, metronome);
    }

    @Override
    public void setInterval(DayInterval interval) {
        currentInterval = interval;
        super.setInterval(interval);
    }
}
