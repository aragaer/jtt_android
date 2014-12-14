package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import dagger.*;
import javax.inject.Singleton;

import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.astronomy.TestCalculator;


@Module(includes=BaseModule.class, overrides=true,
    injects={Clock.class, Chime.class, Metronome.class, TestClock.class,
        Astrolabe.class, TestAstrolabe.class, DayIntervalCalculator.class,
        ClockService.class})
public class TestModule {

    private Metronome metronome = new TestMetronome();

    public void setMetronome(Metronome newMetronome) {
        metronome = newMetronome;
    }

    @Provides @Singleton public Chime getChime() {
        return new TestChime();
    }

    @Provides @Singleton public Metronome getMetronome() {
        return metronome;
    }

    @Provides @Singleton public DayIntervalCalculator getCalculator() {
        return new TestCalculator();
    }
}
