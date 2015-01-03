package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.astronomy.DayIntervalService;
import com.aragaer.jtt.clockwork.AndroidModule;
import com.aragaer.jtt.clockwork.Clock;
import com.aragaer.jtt.clockwork.DateTimeChangeListener;
import com.aragaer.jtt.location.LocationService;
import com.aragaer.jtt.location.AndroidLocationProvider;
import com.aragaer.jtt.location.AndroidLocationChangeNotifier;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;


public class JttService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "JTT_SERVICE";
    private final Clock clock;
    private final DateTimeChangeListener dateTimeChangeListener;
    private final LocationService locationService;
    private final DayIntervalService astrolabe;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public JttService() {
        AndroidModule module = new AndroidModule(this);
        clock = new Clock(module.getChime(), module.getMetronome());
        astrolabe = new DayIntervalService(module.getCalculator());
        dateTimeChangeListener = new DateTimeChangeListener(astrolabe);
        locationService = new LocationService(new AndroidLocationProvider(this),
                new AndroidLocationChangeNotifier(this));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");
        clock.bindToDayIntervalService(astrolabe);
        locationService.registerConsumer(astrolabe);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);

        dateTimeChangeListener.register(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if (key.equals(Settings.PREF_WIDGET)
                || key.equals(Settings.PREF_LOCALE)
                || key.equals(Settings.PREF_HNAME))
            WidgetProvider.draw_all(this);
    }
}
