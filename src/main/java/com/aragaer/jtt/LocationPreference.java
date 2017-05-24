// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.location.*;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.aragaer.jtt.location.android.JttLocationListener;
import com.aragaer.jtt.ui.android.InputFilterMinMax;
import com.aragaer.jtt.ui.android.LocationTextWatcher;


public class LocationPreference extends DialogPreference implements DialogInterface.OnClickListener {
    private float accuracy = 0;
    private LocationManager lm;
    private TextView lat, lon;
    private String latlon;
    public static final String DEFAULT = "0.0:0.0";

    private final static String fmt1 = "%.2f";
    private final static String fmt3 = "%.2f:%.2f";

    private final JttLocationListener locationListener;
    private final LocationTextWatcher textWatcher;

    public LocationPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        locationListener = new JttLocationListener(this);
        textWatcher = new LocationTextWatcher(this);
        latlon = getPersistedString(DEFAULT);
    }

    public CharSequence getLatText() {
        return lat.getText();
    }

    public CharSequence getLonText() {
        return lon.getText();
    }

    @Override protected View onCreateDialogView() {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View locView = li.inflate(R.layout.location_picker, null);

        lat = (TextView) locView.findViewById(R.id.lat);
        lon = (TextView) locView.findViewById(R.id.lon);

        String[] ll = latlon.split(":");
        lat.setText(ll[0]);
        // 66.562222 is the latitude of arctic circle
        lat.setFilters(new InputFilter[]{ new InputFilterMinMax(-66.562222f, 66.562222f) });

        lon.setText(ll[1]);
        lon.setFilters(new InputFilter[]{ new InputFilterMinMax(-180.0f, 180.0f) });

        lat.addTextChangedListener(textWatcher);
        lon.addTextChangedListener(textWatcher);
        return locView;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setNeutralButton(R.string.auto_lat_long, this);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object def) {
        if (restoreValue) {
            latlon = getPersistedString(DEFAULT);
            persistString(latlon);
            setSummary(latlon);
        } else {
            boolean wasNull = latlon == null;
            latlon = (String) def;
            if (!wasNull)
                persistString(latlon);
            setSummary(latlon == null ? latlon : "");
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        switch (id) {
        case Dialog.BUTTON_POSITIVE:
            doSet(latlon);
            persistString(latlon);
            callChangeListener(new String(latlon));
            setSummary(latlon);
            break;
        case Dialog.BUTTON_NEUTRAL:
            acquireLocation();
            break;
        default:
            latlon = getPersistedString("0.0:0.0");
            break;
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
    }

    public void doSet(String l) {
        latlon = l.replace(',', '.'); // force dot as a separator
    }

    public void makeUseOfNewLocation(Location l, boolean stopLocating) {
        if (l.hasAccuracy()) {
            final float new_acc = l.getAccuracy();
            if (accuracy > 0 && accuracy < new_acc)
                return; // this one doesn't have the best accuracy
            accuracy = new_acc;
        }

        lat.setText(String.format(fmt1, l.getLatitude()).replace(',', '.'));
        lon.setText(String.format(fmt1, l.getLongitude()).replace(',', '.'));
        doSet(String.format(fmt3, l.getLatitude(), l.getLongitude()));

        if (stopLocating)
            lm.removeUpdates(locationListener);
    }

    private void acquireLocation() {
        lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(getContext(), R.string.no_providers, Toast.LENGTH_SHORT).show();
            getContext().startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        Location last = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (last != null)
            makeUseOfNewLocation(last, false);

        try {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } catch (IllegalArgumentException e) {
            Log.d("LocationPref", "No network provider");
        }
    }
}
