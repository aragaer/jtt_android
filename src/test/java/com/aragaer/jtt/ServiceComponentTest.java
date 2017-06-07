// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import org.junit.*;

import static org.junit.Assert.*;

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
    }

    private class TestMechanicsModule extends MechanicsModule {

        TestTicker testTicker;

        TestMechanicsModule() {
            super(null);
            testTicker = new TestTicker();
        }

        @Override public Ticker provideTicker() {
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
