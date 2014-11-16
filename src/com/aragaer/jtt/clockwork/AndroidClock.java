package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;

import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.astronomy.SscCalculator;
import com.aragaer.jtt.location.AndroidLocationProvider;
import com.aragaer.jtt.location.LocationProvider;


public class AndroidClock {
    public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";

    public static Clock createFromContext(Context context) {
        LocationProvider provider = new AndroidLocationProvider(context);
        DayIntervalCalculator calculator = new SscCalculator();
        Astrolabe astrolabe = new Astrolabe(calculator, provider, 1);
        Metronome metronome = new AndroidMetronome(context);
        return new Clock(astrolabe, metronome);
    }
}
