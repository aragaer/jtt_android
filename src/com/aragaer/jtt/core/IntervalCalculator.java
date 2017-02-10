// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import java.util.Calendar;


public interface IntervalCalculator {
    public Interval getDayIntervalForJDN(long jdn);
    public void setLocation(float latitude, float longitude);
    public Calendar getSunriseFor(Calendar noon);
    public Calendar getSunsetFor(Calendar noon);
}
