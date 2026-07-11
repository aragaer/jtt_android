// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;


public class TestDayBoundaryHandler implements DayBoundaryHandler {

    private double _depression = 0.8333;

    public void setDepression(double depression) {
        _depression = depression;
    }

    @Override public double getDepression() {
        return _depression;
    }
}
