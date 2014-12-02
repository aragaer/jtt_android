package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.astronomy.DayInterval;

import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;


public class Clock {
    private final Astrolabe astrolabe;
    private final Chime chime;
    private final Clockwork clockwork;
    private final Metronome metronome;

    public Clock(Astrolabe astrolabe, Chime chime, Metronome metronome) {
        this.astrolabe = astrolabe;
        this.chime = chime;
        this.metronome = metronome;
        this.clockwork = new Clockwork();
        this.metronome.attachTo(this.clockwork);
        this.clockwork.attachChime(chime);
        this.clockwork.attachTrigger(new Clockwork.Trigger() {
            public void trigger() {
                Clock.this.adjust();
            }
        }, TICKS_PER_INTERVAL);
    }

    public void adjust() {
        astrolabe.updateLocation();
        DayInterval interval = astrolabe.getCurrentInterval();
		long tickLength = interval.getLength() / TICKS_PER_INTERVAL;
        metronome.start(interval.getStart(), tickLength);
    }
}
