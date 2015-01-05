package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.astronomy.android.AndroidDateTimeChangeListener;
import com.aragaer.jtt.astronomy.DayIntervalService;
import com.aragaer.jtt.astronomy.SscCalculator;
import com.aragaer.jtt.clockwork.android.AndroidChime;
import com.aragaer.jtt.clockwork.android.AndroidMetronome;
import com.aragaer.jtt.clockwork.Clock;
import com.aragaer.jtt.location.AndroidLocationChangeNotifier;
import com.aragaer.jtt.location.AndroidLocationProvider;
import com.aragaer.jtt.location.LocationService;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;


public class JttService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "JTT_SERVICE";
    private final Clock clock;
    private final AndroidDateTimeChangeListener dateTimeChangeListener;
    private final LocationService locationService;
    private AndroidLocationChangeNotifier locationChangeNotifier;
    private final DayIntervalService astrolabe;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public JttService() {
        clock = new Clock(new AndroidChime(this), new AndroidMetronome(this));
        dateTimeChangeListener = new AndroidDateTimeChangeListener();
        astrolabe = new DayIntervalService(new SscCalculator());
        dateTimeChangeListener.setService(astrolabe);
        locationService = new LocationService(new AndroidLocationProvider(this));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");
        astrolabe.registerClient(clock);
        locationChangeNotifier =  new AndroidLocationChangeNotifier(this);
        locationChangeNotifier.setService(locationService);
        locationService.registerClient(astrolabe);

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
