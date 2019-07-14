// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import static org.junit.Assert.*;

import com.aragaer.jtt.astronomy.*;
import com.aragaer.jtt.core.*;
import com.aragaer.jtt.mechanics.*;


public class ServiceComponentTest {

    private static final long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);

    private ServiceComponent serviceComponent;

    @Before public void setUp() {
        TestMechanicsModule mechanicsModule = new TestMechanicsModule();
        serviceComponent = DaggerServiceComponent
            .builder()
            .mechanicsModule(mechanicsModule)
            .astronomyModule(new TestAstronomyModule())
            .build();
    }

    @Test public void testGetTicker() {
        Ticker ticker = serviceComponent.getTicker();
        ticker.start();
        ticker.stop();
        assertEquals("There is only one ticker ever",
                     ticker, serviceComponent.getTicker());
    }

    @Test public void testSolarEventCalculator() {
        SolarEventCalculator calculator = serviceComponent.provideSolarEventCalculator();
        assertNotNull(calculator);
    }

    // TODO: IntervalProvider/IntervalBuilder should not calculate TzOffset itself
    @Test public void testGetIntervalProvider() {
        long timestamp = 0;
        long tzOffset = TimeZone.getDefault().getOffset(timestamp);
        IntervalProvider intervalProvider = serviceComponent.provideIntervalProvider();
        ThreeIntervals intervals = intervalProvider.getIntervalsForTimestamp(0);
        assertFalse("Is night", intervals.isDay());
        assertEquals("Previous sunrise", -MS_PER_DAY*3/4-tzOffset, intervals.getTransitions()[0]);
        assertEquals("Previous sunset", -MS_PER_DAY/4-tzOffset, intervals.getTransitions()[1]);
        assertEquals("Next sunrise", MS_PER_DAY/4-tzOffset, intervals.getTransitions()[2]);
        assertEquals("Next sunset", MS_PER_DAY*3/4-tzOffset, intervals.getTransitions()[3]);
    }

    @Test public void testGetClockwork() {
        Clockwork clockwork = serviceComponent.provideClockwork();
        clockwork.setTime(System.currentTimeMillis());
    }
}

class TestSolarEventCalculator implements SolarEventCalculator {
    @Override public Calendar getSunriseFor(Calendar noon) {
        Calendar result = (Calendar) noon.clone();
        result.add(Calendar.HOUR_OF_DAY, -6);
        return result;
    }
    @Override public Calendar getSunsetFor(Calendar noon) {
        Calendar result = (Calendar) noon.clone();
        result.add(Calendar.HOUR_OF_DAY, 6);
        return result;
    }
}

class TestMechanicsModule extends MechanicsModule {

    private final TestTicker testTicker;

    TestMechanicsModule() {
        super(null);
        testTicker = new TestTicker();
    }

    @Override public Ticker provideTicker(Clockwork clockwork,
                                          Announcer announcer) {
        return testTicker;
    }
}

class TestAstronomyModule extends AstronomyModule {

    TestAstronomyModule() {
        super(null);
    }

    @Override public SolarEventCalculator provideSolarEventCalculator(LocationHandler locationHandler) {
        return new TestSolarEventCalculator();
    }

    @Override public LocationHandler provideLocationHandler() {
        return new TestLocationHandler();
    }
}

class TestTicker implements Ticker {
    @Override public void start() { }
    @Override public void stop() { }
}
