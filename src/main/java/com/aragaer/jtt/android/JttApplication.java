package com.aragaer.jtt.android;

import android.app.Application;
import android.content.*;
import android.os.Build;
import android.preference.PreferenceManager;

import com.aragaer.jtt.*;
import com.aragaer.jtt.astronomy.AstronomyModule;
import com.aragaer.jtt.mechanics.*;
import com.aragaer.jtt.resources.StringResources;


public class JttApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Ticker _ticker;
    private StringResources _stringResources;

    private final BroadcastReceiver _widget_tick_forwarder = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AndroidTicker.ACTION_JTT_TICK.equals(intent.getAction()))
                JTTWidgetProvider.tick(context, intent);
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        JttComponent component = DaggerJttComponent
                .builder()
                .astronomyModule(new AstronomyModule(this))
                .mechanicsModule(new MechanicsModule(this))
                .build();
        _ticker = component.getTicker();

        registerReceiver(_widget_tick_forwarder, new IntentFilter(AndroidTicker.ACTION_JTT_TICK));

        _stringResources = new StringResources(this);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);

        toggle_notify(pref.getBoolean(Settings.PREF_NOTIFY, true));
    }

    public StringResources getStringResources() {
        return _stringResources;
    }

    public void startTicker() {
        _ticker.start();
    }

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        switch (key) {
            case Settings.PREF_NOTIFY:
                toggle_notify(pref.getBoolean(Settings.PREF_NOTIFY, true));
                break;
            case Settings.PREF_LOCATION:
                startTicker();
                break;
            case Settings.PREF_WIDGET:
            case Settings.PREF_LOCALE:
            case Settings.PREF_HNAME:
            case Settings.PREF_EMOJI_WIDGET:
                JTTWidgetProvider.draw_all(this);
                break;
        }
    }

    private void toggle_notify(boolean notify) {
        Intent statusIntent = new Intent(this, JttStatus.class);
        if (notify) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(statusIntent);
            else
                startService(statusIntent);
        } else
            stopService(statusIntent);
    }
}
