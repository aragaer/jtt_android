package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.astronomy.SscCalculator;
import com.aragaer.jtt.location.AndroidLocationProvider;
import com.aragaer.jtt.location.LocationProvider;
import com.aragaer.jtt.core.JttTime;


public class AndroidClock implements Clock {
    private final Astrolabe astrolabe;
    private final Clockwork clockwork;
    private final Metronome metronome;

    public AndroidClock(Context context) {
        LocationProvider provider = new AndroidLocationProvider(context);
        DayIntervalCalculator calculator = new SscCalculator();
        astrolabe = new Astrolabe(calculator, provider, 1);
        clockwork = new Clockwork();
        metronome = new AndroidMetronome(context);
        metronome.attachTo(clockwork);
    }

    public void adjust() {
        astrolabe.updateLocation();
        DayInterval interval = astrolabe.getCurrentInterval();
		long tickLength = interval.getLength() / JttTime.TICKS_PER_INTERVAL;
        metronome.start(interval.getStart(), tickLength);
    }
}
