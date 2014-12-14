package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import dagger.ObjectGraph;
import javax.inject.Inject;

import com.aragaer.jtt.location.AndroidLocationProvider;
import com.aragaer.jtt.location.AndroidLocationChangeNotifier;
import com.aragaer.jtt.location.LocationService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class ClockService extends Service {

    @Inject Clock clock;
    @Inject Astrolabe astrolabe;
    LocationService locationService;

    public ClockService() {
        ObjectGraph graph = ObjectGraph.create(new AndroidModule(this));
        clock = graph.get(Clock.class);
        astrolabe = graph.get(Astrolabe.class);
        AndroidLocationProvider provider = new AndroidLocationProvider(this);
        AndroidLocationChangeNotifier changeNotifier = new AndroidLocationChangeNotifier(this);
        locationService = new LocationService(provider, changeNotifier);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        clock.bindToAstrolabe(astrolabe);
        locationService.registerConsumer(astrolabe);

        return START_STICKY;
    }
}
