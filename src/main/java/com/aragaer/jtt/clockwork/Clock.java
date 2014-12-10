package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import javax.inject.Inject;

import com.aragaer.jtt.astronomy.DayInterval;

import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;


public class Clock {
    private Astrolabe astrolabe;
    private final Chime chime;
    private final Metronome metronome;

    @Inject public Clock(Chime chime, Metronome metronome) {
        this.chime = chime;
        this.metronome = metronome;
        this.metronome.attachTo(this);
    }

    private int lastTick;

    public void tick(int ticks) {
        if (TICKS_PER_INTERVAL - ticks < lastTick % TICKS_PER_INTERVAL)
            astrolabe.onIntervalEnded();
        else
            lastTick += ticks;
        chime.ding(lastTick);
    }

    public void setInterval(DayInterval interval) {
		long tickLength = interval.getLength() / TICKS_PER_INTERVAL;
        if (interval.isDay())
            lastTick = TICKS_PER_INTERVAL;
        else
            lastTick = 0;
        metronome.start(interval.getStart(), tickLength);
    }

    public void bindToAstrolabe(Astrolabe newAstrolabe) {
        astrolabe = newAstrolabe;
        astrolabe.bindToClock(this);
    }
}
