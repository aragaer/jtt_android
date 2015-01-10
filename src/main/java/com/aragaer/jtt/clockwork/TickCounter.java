package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;


public class TickCounter {
    private int teethPassed;
    private TickClient client;

    /* package private */ void switchToNightGear() {
        teethPassed = 0;
    }

    /* package private */ void switchToDayGear() {
        teethPassed = TICKS_PER_INTERVAL;
    }

    public void set(int count) {
        if (client != null)
            client.tickChanged(teethPassed+count);
    }

    public void addClient(TickClient client) {
        this.client = client;
    }
}
