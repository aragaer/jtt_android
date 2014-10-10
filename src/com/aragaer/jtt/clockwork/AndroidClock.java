package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;

import com.aragaer.jtt.core.DayInterval;
import com.aragaer.jtt.core.Hour;


public class AndroidClock implements Clock {
    private final Context context;
    private final AndroidClockwork clockwork;
    private final Metronome metronome;

    public AndroidClock(Context context) {
        this.context = context;
        clockwork = new AndroidClockwork(context);
        metronome = new AndroidMetronome(context);
        metronome.attachTo(clockwork);
    }

    public void adjust() {
        DayInterval interval = clockwork.getCurrentInterval();
		long tickLength = interval.getLength() / Hour.INTERVAL_TICKS;
        metronome.start(interval.getStart(), tickLength);
    }
}
