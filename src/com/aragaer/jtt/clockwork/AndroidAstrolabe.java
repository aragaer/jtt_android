package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.ContentValues;
import android.content.Context;

import com.aragaer.jtt.Location;
import com.aragaer.jtt.Settings;
import com.aragaer.jtt.core.DayInterval;
import com.aragaer.jtt.core.ThreeIntervals;
import com.aragaer.jtt.core.TransitionProvider;


public class AndroidAstrolabe implements Astrolabe {
    private final Context context;

    public AndroidAstrolabe(Context context) {
        this.context = context;
    }

    public void updateLocation() {
        Location location = Settings.getLocation(context);
        ContentValues locationCV = new ContentValues(2);
        locationCV.put("lat", location.getLatitude());
        locationCV.put("lon", location.getLongitude());
        context.getContentResolver().update(TransitionProvider.LOCATION, locationCV, null, null);
    }

    public DayInterval getCurrentInterval() {
        ThreeIntervals transitions = TransitionProvider.getSurroundingTransitions(context);
        return transitions.getCurrent();
    }
}
