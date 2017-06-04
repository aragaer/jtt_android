// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import org.junit.*;

import com.aragaer.jtt.astronomy.*;

import static org.junit.Assert.*;


public class JttTest {

    @Test public void testGetSolarEventCalculator() {
        JttComponent jttComponent = Jtt.getJttComponent();
        assertNotNull("Actually get jtt component", jttComponent);
        assertEquals("Have only single instance of jtt component",
                     jttComponent, Jtt.getJttComponent());

        SolarEventCalculator calculator = jttComponent.provideSolarEventCalculator();
        assertEquals("Jtt component provides SscAdapter as calculator",
                     SscAdapter.class, calculator.getClass());
        assertEquals("There is single instance of calculator",
                     jttComponent.provideSolarEventCalculator(), calculator);
    }
}
