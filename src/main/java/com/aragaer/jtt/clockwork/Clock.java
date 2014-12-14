package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import javax.inject.Inject;

import com.aragaer.jtt.astronomy.DayInterval;

import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;


public class Clock {
    private final Chime chime;
    private final Metronome metronome;
    private final Cogs cogs;

    @Inject public Clock(Chime chime, Metronome metronome) {
        this.chime = chime;
        this.cogs = new Cogs();
        this.cogs.attachChime(chime);
        this.metronome = metronome;
        this.metronome.attachTo(cogs);
    }

    public Cogs getCogs() {
        return cogs;
    }

    public void setInterval(DayInterval interval) {
		long tickLength = interval.getLength() / TICKS_PER_INTERVAL;
        if (interval.isDay())
            cogs.switchToDayGear();
        else
            cogs.switchToNightGear();
        metronome.start(interval.getStart(), tickLength);
    }

    public void bindToAstrolabe(Astrolabe astrolabe) {
        cogs.bindToAstrolabe(astrolabe);
        astrolabe.bindToClock(this);
    }
}
