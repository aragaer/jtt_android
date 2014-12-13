package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import dagger.ObjectGraph;
import javax.inject.Inject;

import com.aragaer.jtt.clockwork.AndroidModule;
import com.aragaer.jtt.clockwork.Astrolabe;
import com.aragaer.jtt.clockwork.Clock;
import com.aragaer.jtt.clockwork.DateTimeChangeListener;
import com.aragaer.jtt.location.LocationProvider;
import com.aragaer.jtt.location.AndroidLocationProvider;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class JttService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "JTT_SERVICE";
    @Inject Clock clock;
    @Inject DateTimeChangeListener dateTimeChangeListener;
    private LocationProvider locationProvider;
    @Inject Astrolabe astrolabe;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public JttService() {
        ObjectGraph graph = ObjectGraph.create(new AndroidModule(this));
        clock = graph.get(Clock.class);
        astrolabe = graph.get(Astrolabe.class);
        dateTimeChangeListener = graph.get(DateTimeChangeListener.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");
        clock.bindToAstrolabe(astrolabe);
        locationProvider = new AndroidLocationProvider(this);
        locationProvider.setAstrolabe(astrolabe);
        locationProvider.postInit();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);

        dateTimeChangeListener.register(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if (key.equals(Settings.PREF_LOCATION))
            astrolabe.onLocationChanged(locationProvider.getCurrentLocation());
        else if (key.equals(Settings.PREF_WIDGET)
                || key.equals(Settings.PREF_LOCALE)
                || key.equals(Settings.PREF_HNAME))
            WidgetProvider.draw_all(this);
    }
}
