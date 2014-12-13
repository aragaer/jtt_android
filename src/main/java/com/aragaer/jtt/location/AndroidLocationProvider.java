package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aragaer.jtt.Settings;


public class AndroidLocationProvider extends LocationProvider
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final Context context;

    public AndroidLocationProvider(Context context) {
        this.context = context;
        PreferenceManager.getDefaultSharedPreferences(context)
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public Location getCurrentLocation() {
        return Settings.getLocation(context);
    }

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        postInit();
    }
}
