package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import dagger.ObjectGraph;
import javax.inject.Inject;

import com.aragaer.jtt.location.AndroidLocationProvider;
import com.aragaer.jtt.location.LocationProvider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class ClockService extends Service {

    @Inject Clock clock;
    @Inject Astrolabe astrolabe;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        clock.bindToAstrolabe(astrolabe);
        LocationProvider locationProvider = new AndroidLocationProvider(this, astrolabe);
        locationProvider.postInit();

        return START_STICKY;
    }
}
