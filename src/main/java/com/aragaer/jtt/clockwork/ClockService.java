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
    LocationProvider locationProvider;

    public ClockService() {
        ObjectGraph graph = ObjectGraph.create(new AndroidModule(this));
        clock = graph.get(Clock.class);
        astrolabe = graph.get(Astrolabe.class);
        locationProvider = new AndroidLocationProvider(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        clock.bindToAstrolabe(astrolabe);
        locationProvider.setAstrolabe(astrolabe);
        locationProvider.postInit();

        return START_STICKY;
    }
}
