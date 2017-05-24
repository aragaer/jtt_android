// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.location.android;

import com.aragaer.jtt.LocationPreference;

import android.location.*;
import android.os.Bundle;


public class JttLocationListener implements LocationListener {
    private final LocationPreference pref;

    public JttLocationListener(LocationPreference pref) {
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
};
