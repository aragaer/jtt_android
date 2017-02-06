// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.android;

import com.aragaer.jtt.core.*;

import android.database.Cursor;
import android.util.Log;


public class AndroidIntervalProvider implements IntervalProvider {

    private static AndroidIntervalProvider instance;
    private final SscCalculator _ssc;


    private AndroidIntervalProvider() {
        _ssc = new SscCalculator();
    }

    public static AndroidIntervalProvider getInstance() {
        if (instance == null)
            instance = new AndroidIntervalProvider();
        return instance;
    }

    @Override public ThreeIntervals getIntervalsForTimestamp(long time) {
        return _ssc.getSurroundingIntervalsForTimestamp(time);
    }

    public void move(float latitude, float longitude) {
        _ssc.setLocation(latitude, longitude);
    }
}
