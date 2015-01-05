package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalClient;

import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;


public class TickService implements DayIntervalClient {
    private final TickProvider metronome;
    private final TickCounter counter;

    public TickService(TickProvider metronome) {
        this.metronome = metronome;
        counter = new TickCounter();
        metronome.attachTo(counter);
    }

    public void intervalChanged(DayInterval interval) {
		long tickLength = interval.getLength() / TICKS_PER_INTERVAL;
        if (interval.isDay())
            counter.switchToDayGear();
        else
            counter.switchToNightGear();
        metronome.start(interval.getStart(), tickLength);
    }

    public void registerClient(TickClient client) {
        counter.addClient(client);
    }
}
