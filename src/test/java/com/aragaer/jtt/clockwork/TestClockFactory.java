package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import dagger.*;
import javax.inject.Singleton;


@Module(injects={Clock.class, Chime.class, Metronome.class, TestClock.class})
public class TestClockFactory {

    private final Metronome metronome;

    public TestClockFactory() {
        metronome = new TestMetronome();
    }

    public TestClockFactory(Metronome metronome) {
        this.metronome = metronome;
    }

    @Provides @Singleton public Chime getChime() {
        return new TestChime();
    }

    @Provides @Singleton public Metronome getMetronome() {
        return metronome;
    }
}
