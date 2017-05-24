// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.location.*;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.aragaer.jtt.location.android.JttLocationListener;
import com.aragaer.jtt.ui.android.InputFilterMinMax;


public class LocationPreference extends DialogPreference implements
                                                             TextWatcher, DialogInterface.OnClickListener {
    private float accuracy = 0;
    private LocationManager lm;
    private TextView lat, lon;
    private LinearLayout locView;
    private String latlon;
    public static final String DEFAULT = "0.0:0.0";

    private final static String fmt1 = "%.2f";
    private final static String fmt2 = "%s:%s";
    private final static String fmt3 = "%.2f:%.2f";

    private final JttLocationListener locationListener;

    public LocationPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        locationListener = new JttLocationListener(this);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        locView = (LinearLayout) li.inflate(R.layout.location_picker, null);

        lat = (TextView) locView.findViewById(R.id.lat);
        lon = (TextView) locView.findViewById(R.id.lon);

        if (latlon == null)
            latlon = getPersistedString(DEFAULT);
        String[] ll = latlon.split(":");
        lat.setText(ll[0]);
        // 66.562222 is the latitude of arctic circle
        lat.setFilters(new InputFilter[]{ new InputFilterMinMax(-66.562222f, 66.562222f) });

        lon.setText(ll[1]);
        lon.setFilters(new InputFilter[]{ new InputFilterMinMax(-180.0f, 180.0f) });

        lat.addTextChangedListener(this);
        lon.addTextChangedListener(this);
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
        default:
            latlon = getPersistedString("0.0:0.0");
            break;
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        Button pos = ((AlertDialog) getDialog())
            .getButton(Dialog.BUTTON_NEUTRAL);
        pos.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    acquireLocation();
                }
            });
    }

    private void doSet(String l) {
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
        lm = (LocationManager) getContext().getSystemService(
                                                             Context.LOCATION_SERVICE);

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

    public void afterTextChanged(Editable arg0) {
        CharSequence latt = lat.getText();
        CharSequence lont = lon.getText();
        if (latt != null && lont != null)
            doSet(String.format(fmt2, latt, lont));
    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }
}
