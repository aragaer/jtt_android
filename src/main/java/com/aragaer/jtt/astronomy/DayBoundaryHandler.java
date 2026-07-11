// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;


public interface DayBoundaryHandler {
    /* Solar depression angle in degrees below the horizon
     * marking the day/night boundary. */
    double getDepression();
}
