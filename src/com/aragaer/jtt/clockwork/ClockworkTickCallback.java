package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.core.TickCallback;


public class ClockworkTickCallback implements TickCallback {

    private final Clockwork clockwork;

    public ClockworkTickCallback(Clockwork clockwork) {
        this.clockwork = clockwork;
    }

    public void onTick() {
        clockwork.tick();
    }
}
