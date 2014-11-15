package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;

import com.aragaer.jtt.core.TickService;


public class AndroidMetronome implements Metronome {
    private Context context;

    public AndroidMetronome(Context context) {
        this.context = context;
    }

    public void attachTo(Clockwork clockwork) {
		TickService.setCallback(new ClockworkTickCallback(clockwork));
    }

	public void start(long start, long tickLength) {
		TickService.start(context, start, tickLength);
	}

	public void stop() {
		TickService.stop(context);
	}
}
