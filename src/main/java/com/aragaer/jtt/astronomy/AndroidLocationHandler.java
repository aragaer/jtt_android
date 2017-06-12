// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aragaer.jtt.Settings;


public class AndroidLocationHandler implements LocationHandler {

    private final Context _context;
    private final SharedPreferences _pref;
    private final static String SEPARATOR = ":";
    private final static String FORMAT = "%.2f"+SEPARATOR+"%.2f";

    public AndroidLocationHandler(Context context) {
        _context = context;
        _pref = PreferenceManager.getDefaultSharedPreferences(_context);
    }

    public void setLocation(float latitude, float longitude) {
        _pref.edit()
            .putString(Settings.PREF_LOCATION,
                       String.format(FORMAT, latitude, longitude))
            .commit();
    }

    public float[] getLocation() {
        String[] ll = _pref.getString(Settings.PREF_LOCATION, "").split(SEPARATOR);
        if (ll.length == 2)
            try {
                return new float[] { Float.parseFloat(ll[0]), Float.parseFloat(ll[1]) };
            } catch (NumberFormatException e) {
                // do nothing - will return default value
            }
        return new float[] { 0, 0 };
    }
}
