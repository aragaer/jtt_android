package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class ClockService extends Service {

    private Clock clock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static Astrolabe astrolabe;
    private static Chime chime;

    /* package private */ static void overrideAstrolabe(Astrolabe newAstrolabe) {
        astrolabe = newAstrolabe;
    }

    /* package private */ static void overrideChime(Chime newChime) {
        chime = newChime;
    }

    private Astrolabe getAstrolabe(ComponentFactory components) {
        return astrolabe == null ? components.getAstrolabe() : astrolabe;
    }

    private Chime getChime(ComponentFactory components) {
        return chime == null ? components.getChime() : chime;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ComponentFactory components = new AndroidClockFactory(this);
        clock = new Clock(getAstrolabe(components), getChime(components), components.getMetronome());
        clock.adjust();

        return START_STICKY;
    }
}
