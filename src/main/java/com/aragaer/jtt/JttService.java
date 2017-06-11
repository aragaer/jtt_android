// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import com.aragaer.jtt.astronomy.SolarEventCalculator;
import com.aragaer.jtt.mechanics.*;

import android.app.Service;
import android.content.*;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;


public class JttService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "JTT_SERVICE";
    private JttStatus status_notify;
    private Ticker ticker;
    private ServiceComponent component;

    @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();
        component = DaggerServiceComponent
            .builder()
            .mechanicsModule(new MechanicsModule(this))
            .build();
        ticker = component.getTicker();
        registerReceiver(on, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(off, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        Log.i(TAG, "Service created");
    }

    @Override public int onStartCommand(Intent intent, int flags, int startid) {
        Log.i(TAG, "Service starting");
        move();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);

        toggle_notify(pref.getBoolean("jtt_notify", true));

        return START_STICKY;
    }

    private void toggle_notify(final boolean notify) {
        if (status_notify == null) {
            if (notify)
                status_notify = new JttStatus(this);
        } else {
            if (!notify) {
                status_notify.release();
                status_notify = null;
            }
        }
    }

    private final BroadcastReceiver on = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                move();
            }
        };
    private final BroadcastReceiver off = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                ticker.stop();
            }
        };

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if (key.equals(Settings.PREF_NOTIFY))
            toggle_notify(pref.getBoolean("jtt_notify", true));
        else if (key.equals(Settings.PREF_LOCATION))
            move();
        else if (key.equals(Settings.PREF_WIDGET)
                 || key.equals(Settings.PREF_LOCALE)
                 || key.equals(Settings.PREF_HNAME))
            JTTWidgetProvider.draw_all(this);
    }

    private void move() {
        float l[] = Settings.getLocation(this);
        SolarEventCalculator calculator = component.provideSolarEventCalculator();
        calculator.setLocation(l[0], l[1]);
        ticker.start();
    }
}
