package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class AndroidLocationChangeNotifier implements LocationChangeNotifier,
       SharedPreferences.OnSharedPreferenceChangeListener {
    private Context context;
    private LocationService service;

    public AndroidLocationChangeNotifier(Context context) {
        this.context = context;
        register();
    }

    public void setService(LocationService service) {
        this.service = service;
    }

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        service.locationChanged();
    }

    void register() {
        PreferenceManager.getDefaultSharedPreferences(context)
            .registerOnSharedPreferenceChangeListener(this);
    }
}
