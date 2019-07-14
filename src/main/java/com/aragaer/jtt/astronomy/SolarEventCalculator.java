// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;

import java.util.Calendar;


public interface SolarEventCalculator {
    Calendar getSunriseFor(Calendar noon);
    Calendar getSunsetFor(Calendar noon);
}
