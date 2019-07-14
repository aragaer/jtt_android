// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import com.aragaer.jtt.astronomy.AstronomyModule;
import com.aragaer.jtt.mechanics.MechanicsModule;
import com.aragaer.jtt.mechanics.Ticker;

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
            .astronomyModule(new AstronomyModule(this))
            .mechanicsModule(new MechanicsModule(this))
            .build();
        ticker = component.getTicker();
        registerReceiver(on, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(off, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        Log.i(TAG, "Service created");
    }

    @Override public void onDestroy() {
        if (status_notify != null) {
            status_notify.release();
            status_notify = null;
        }
        unregisterReceiver(on);
        unregisterReceiver(off);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startid) {
        Log.i(TAG, "Service starting");
        ticker.start();

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
        Log.i("jtt", "Toggle notify to "+status_notify);
    }

    private final BroadcastReceiver on = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                ticker.start();
            }
        };
    private final BroadcastReceiver off = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                ticker.stop();
            }
        };

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if (key.equals(Settings.PREF_NOTIFY))
            toggle_notify(pref.getBoolean(Settings.PREF_NOTIFY, true));
        else if (key.equals(Settings.PREF_LOCATION))
            ticker.start();
        else if (key.equals(Settings.PREF_WIDGET)
                 || key.equals(Settings.PREF_LOCALE)
                 || key.equals(Settings.PREF_HNAME)
                 || key.equals(Settings.PREF_EMOJI_WIDGET))
            JTTWidgetProvider.draw_all(this);
    }
}
