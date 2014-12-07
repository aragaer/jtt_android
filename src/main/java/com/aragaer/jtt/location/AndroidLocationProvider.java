package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;

import com.aragaer.jtt.Settings;


public class AndroidLocationProvider implements LocationProvider {
    private final Context context;

    public AndroidLocationProvider(Context context) {
        this.context = context;
    }

    @Override
    public Location getCurrentLocation() {
        return Settings.getLocation(context);
    }
}
