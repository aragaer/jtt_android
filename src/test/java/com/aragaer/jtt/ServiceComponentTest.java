// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import java.util.Calendar;

import org.junit.*;

import static org.junit.Assert.*;

import com.aragaer.jtt.astronomy.*;
import com.aragaer.jtt.core.*;
import com.aragaer.jtt.mechanics.*;


public class ServiceComponentTest {

    private ServiceComponent serviceComponent;
    private TestMechanicsModule mechanicsModule;

    @Before public void setUp() {
        mechanicsModule = new TestMechanicsModule();
        serviceComponent = DaggerServiceComponent
            .builder()
            .mechanicsModule(mechanicsModule)
            .build();
    }

    @Test public void testGetTicker() {
        Ticker ticker = serviceComponent.getTicker();
        ticker.start();
        ticker.stop();
        assertEquals("There is only one ticker ever",
                     ticker, serviceComponent.getTicker());
    }

    @Test public void testGetSolarEventCalculator() {
        SolarEventCalculator calculator = serviceComponent.provideSolarEventCalculator();
        assertEquals("Jtt component provides SscAdapter as calculator",
                     SscAdapter.class, calculator.getClass());
    }

    @Test public void testGetIntervalProvider() {
        IntervalProvider intervalProvider = serviceComponent.provideIntervalProvider();
        assertNotNull("Provider is correctly initialized",
                      intervalProvider.getIntervalsForTimestamp(System.currentTimeMillis()));
    }

    @Test public void testGetClockwork() {
        Clockwork clockwork = serviceComponent.provideClockwork();
        clockwork.setTime(System.currentTimeMillis());
    }

    // TODO: Use this class to feed some test data to other dependencies
    private class TestSolarEventCalculator implements SolarEventCalculator {
        @Override public void setLocation(float latitude, float longitude) {
        }
        @Override public Calendar getSunriseFor(Calendar noon) {
            return null;
        }
        @Override public Calendar getSunsetFor(Calendar noon) {
            return null;
        }
    }

    private class TestMechanicsModule extends MechanicsModule {

        TestTicker testTicker;

        TestMechanicsModule() {
            super(null);
            testTicker = new TestTicker();
        }

        @Override public Ticker provideTicker(Clockwork clockwork,
                                              IntervalProvider provider) {
            return testTicker;
        }
    }

    private class TestTicker implements Ticker {
        @Override public void start() {
        }

        @Override public void stop() {
        }
    }
}
