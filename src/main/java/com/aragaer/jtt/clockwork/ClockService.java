package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import dagger.ObjectGraph;

import com.aragaer.jtt.location.AndroidLocationProvider;
import com.aragaer.jtt.location.LocationProvider;

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
        LocationProvider locationProvider = new AndroidLocationProvider(this);
        ObjectGraph graph = ObjectGraph.create(new AndroidClockFactory(this));
        clock = graph.get(Clock.class);
        graph.get(Astrolabe.class).onLocationChanged(locationProvider.getCurrentLocation());

        return START_STICKY;
    }
}
