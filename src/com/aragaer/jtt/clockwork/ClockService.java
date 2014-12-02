package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class ClockService extends Service {

    private static ComponentFactory components;

    private Metronome metronome;
    private Clock clock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* package private */ static void setComponentFactory(ComponentFactory newComponents) {
        components = newComponents;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        metronome = new AndroidMetronome(this);

        clock = new Clock(components.getAstrolabe(), components.getChime(), metronome);
        clock.adjust();

        return START_STICKY;
    }
}
