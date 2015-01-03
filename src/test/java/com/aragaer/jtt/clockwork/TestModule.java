package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.astronomy.DayIntervalService;
import com.aragaer.jtt.astronomy.TestDayIntervalService;
import com.aragaer.jtt.astronomy.TestCalculator;


public class TestModule {

    private Metronome metronome = new TestMetronome();

    public void setMetronome(Metronome newMetronome) {
        metronome = newMetronome;
    }

    public Chime getChime() {
        return new TestChime();
    }

    public Metronome getMetronome() {
        return metronome;
    }

    public DayIntervalCalculator getCalculator() {
        return new TestCalculator();
    }
}
