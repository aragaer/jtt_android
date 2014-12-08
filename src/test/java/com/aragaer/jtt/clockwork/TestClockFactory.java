package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import dagger.*;

@Module(injects=Clock.class)
public class TestClockFactory implements ComponentFactory {

    private Astrolabe astrolabe;
    private Chime chime;
    private Metronome metronome;

    public TestClockFactory() {
        astrolabe = new TestAstrolabe();
        chime = new TestChime();
        metronome = new TestMetronome();
    }

    @Provides public Chime getChime() {
        return chime;
    }

    public Astrolabe getAstrolabe() {
        return astrolabe;
    }

    @Provides public Metronome getMetronome() {
        return metronome;
    }
}
