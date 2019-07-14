// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.location.android;

import com.aragaer.jtt.LocationPreference;

import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.aragaer.jtt.R;


public class JttLocationListener implements LocationListener {
    private final Context context;
    private final LocationPreference pref;

    public JttLocationListener(Context context, LocationPreference pref) {
        this.context = context;
        this.pref = pref;
    }

    @Override public void onLocationChanged(Location location) {
        pref.makeUseOfNewLocation(location, true);
    }

    @Override public void onProviderDisabled(String provider) {
    }

    @Override public void onProviderEnabled(String provider) {
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void acquireLocation() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(context, R.string.no_providers, Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        Location last = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (last != null)
            pref.makeUseOfNewLocation(last, false);

        try {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } catch (IllegalArgumentException e) {
            Log.d("LocationPref", "No network provider");
        }
    }

    public void stopLocating() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        lm.removeUpdates(this);
    }
}
