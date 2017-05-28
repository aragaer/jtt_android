// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

    public final JttLocationListener locationListener;
    private final LocationTextWatcher textWatcher;
    private final Context context;

    public LocationPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        context = ctx;
        locationListener = new JttLocationListener(ctx, this);
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
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        latlon = restoreValue ? getPersistedString(DEFAULT) : (String) def;
        persistString(latlon);
        setSummary(latlon == null ? "" : latlon);
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
            latlon = getPersistedString(DEFAULT);
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

        if (lat == null)
            throw new RuntimeException("Latitude field is not created");
        if (lon == null)
            throw new RuntimeException("Longitude field is not created");
        lat.setText(String.format(fmt1, l.getLatitude()).replace(',', '.'));
        lon.setText(String.format(fmt1, l.getLongitude()).replace(',', '.'));
        doSet(String.format(fmt3, l.getLatitude(), l.getLongitude()));

        if (stopLocating)
            lm.removeUpdates(locationListener);
    }

    private void acquireLocation() {
        locationListener.acquireLocation();
    }
}
