package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;


public class AndroidMetronome implements Metronome {
    private Context context;
    private Clock clock;
    private long stopAt = -1;

    public AndroidMetronome(Context context) {
        this.context = context;
    }

    public void attachTo(Clock newClock) {
        clock = newClock;
    }

	public void start(long start, long tickLength) {
		TickService.setCallback(new ClockworkTickCallback(clock, start, tickLength, stopAt));
		TickService.start(context, start, tickLength);
	}

    public void setStopTime(long stopAt) {
        this.stopAt = stopAt;
    }

	public void stop() {
		TickService.stop(context);
	}
}
