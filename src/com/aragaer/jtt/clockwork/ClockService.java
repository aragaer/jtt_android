package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.location.LocationProvider;
import com.aragaer.jtt.core.JttTime;


public class ClockService extends Service {

    private static ComponentFactory components;

    private Astrolabe astrolabe;
    private Clockwork clockwork;
    private Metronome metronome;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* package private */ static void setComponentFactory(ComponentFactory newComponents) {
        components = newComponents;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        astrolabe = components.getAstrolabe();
        metronome = new AndroidMetronome(this);
        clockwork = new Clockwork();

        clockwork.attachChime(components.getChime());
        metronome.attachTo(clockwork);

        astrolabe.updateLocation();
        DayInterval interval = astrolabe.getCurrentInterval();
		long tickLength = interval.getLength() / JttTime.TICKS_PER_INTERVAL;
        if (interval.isDay())
            clockwork.setTo(JttTime.TICKS_PER_INTERVAL);
        else
            clockwork.setTo(0);
        metronome.start(interval.getStart(), tickLength);

        return START_STICKY;
    }
}
