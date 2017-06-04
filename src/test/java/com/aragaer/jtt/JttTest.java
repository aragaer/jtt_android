// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import org.junit.*;

import com.aragaer.jtt.astronomy.*;
import com.aragaer.jtt.core.*;

import static org.junit.Assert.*;


public class JttTest {

    private JttComponent jttComponent;

    @Before public void setUp() {
        jttComponent = Jtt.getJttComponent();
    }

    @Test public void testSingleton() {
        assertEquals("Have only single instance of jtt component",
                     jttComponent, Jtt.getJttComponent());
    }

    @Test public void testGetSolarEventCalculator() {
        SolarEventCalculator calculator = jttComponent.provideSolarEventCalculator();
        assertEquals("Jtt component provides SscAdapter as calculator",
                     SscAdapter.class, calculator.getClass());
        assertEquals("There is single instance of calculator",
                     jttComponent.provideSolarEventCalculator(), calculator);
    }

    @Test public void testGetIntervalProvider() {
        IntervalProvider intervalProvider = jttComponent.provideIntervalProvider();
        assertNotNull("Provider is correctly initialized",
                      intervalProvider.getIntervalsForTimestamp(System.currentTimeMillis()));
    }

    @Test public void testGetClockwork() {
        Clockwork clockwork = jttComponent.provideClockwork();
        clockwork.setTime(System.currentTimeMillis());
    }
}
