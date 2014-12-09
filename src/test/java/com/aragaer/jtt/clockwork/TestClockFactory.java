package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import dagger.*;
import javax.inject.Singleton;

import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.astronomy.TestCalculator;


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

    @Module(injects={Astrolabe.class, DayIntervalCalculator.class, TestAstrolabe.class}, overrides=true)
    public static class TestAstrolabeModule {

        private TestClock clock;

        public TestAstrolabeModule(TestClock clock) {
            this.clock = clock;
        }

        @Provides @Singleton public Clock getClock() {
            return clock;
        }

        @Provides @Singleton public DayIntervalCalculator getCalculator() {
            return new TestCalculator();
        }
    }
}
