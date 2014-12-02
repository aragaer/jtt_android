package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import java.util.ArrayList;
import java.util.List;


public class Clockwork {

    /* package private */ interface Trigger {
        public void trigger();
    }

    private int lastTick;
    private Chime chime;
    private Trigger trigger;
    private int triggerPeriod;

    private int lastTriggerAt(int time) {
        return time - time % triggerPeriod;
    }

    public void tick(int ticks) {
        int newTick = lastTick + ticks;
        if (trigger != null && lastTriggerAt(newTick) != lastTriggerAt(lastTick))
            trigger.trigger();
        lastTick = newTick;
        if (chime != null)
            chime.ding(lastTick);
    }

    public void setTo(int initialState) {
        lastTick = initialState;
    }

    public void attachChime(Chime chime) {
        this.chime = chime;
    }

    public void attachTrigger(Trigger trigger, int period) {
        this.trigger = trigger;
        triggerPeriod = period;
    }
}
