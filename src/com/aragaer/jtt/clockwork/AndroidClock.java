package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.core.JttTime;


public class AndroidClock implements Clock {
    private final Context context;
    private final Astrolabe astrolabe;
    private final AndroidClockwork clockwork;
    private final Metronome metronome;

    public AndroidClock(Context context) {
        this.context = context;
        astrolabe = new AndroidAstrolabe(context);
        clockwork = new AndroidClockwork(context);
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
