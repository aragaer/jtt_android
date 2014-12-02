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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        clock = new Clock(new AndroidClockFactory(this));
        clock.adjust();

        return START_STICKY;
    }
}
