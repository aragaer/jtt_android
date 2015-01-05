package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;


public class TickCounter {
    private int teethPassed;
    private TickClient client;

    public void rotate(int teeth) {
        if (TICKS_PER_INTERVAL - teeth > teethPassed % TICKS_PER_INTERVAL)
            teethPassed += teeth;
        if (client != null)
            client.tickChanged(teethPassed);
    }

    /* package private */ void switchToNightGear() {
        teethPassed = 0;
    }

    /* package private */ void switchToDayGear() {
        teethPassed = TICKS_PER_INTERVAL;
    }

    public void addClient(TickClient client) {
        this.client = client;
    }
}
