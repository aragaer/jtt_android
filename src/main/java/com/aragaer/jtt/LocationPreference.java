// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import android.app.Dialog;
import android.content.*;
import android.location.Location;
import android.preference.DialogPreference;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aragaer.jtt.ui.android.InputFilterMinMax;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;


public class LocationPreference extends DialogPreference implements DialogInterface.OnClickListener {
    private TextView lat, lon;
    private String latlon;
    static final String DEFAULT = "0.0:0.0";

    private final static String fmt3 = "%.2f:%.2f";

    public LocationPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        latlon = getPersistedString(DEFAULT);
    }

    public float getLat() {
        try {
            return Float.parseFloat(lat.getText().toString());
        } catch (NumberFormatException ex) {
            return 0f;
        }
    }

    public float getLon() {
        try {
            return Float.parseFloat(lon.getText().toString());
        } catch (NumberFormatException ex) {
            return 0f;
        }
    }

    @Override protected View onCreateDialogView() {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (li == null)
            return null;
        View locView = li.inflate(R.layout.location_picker, null);

        lat = locView.findViewById(R.id.lat);
        lon = locView.findViewById(R.id.lon);

        String[] ll = latlon.split(":");
        lat.setText(ll[0]);
        // 66.562222 is the latitude of arctic circle
        lat.setFilters(new InputFilter[]{ new InputFilterMinMax(-66.562222f, 66.562222f) });

        lon.setText(ll[1]);
        lon.setFilters(new InputFilter[]{ new InputFilterMinMax(-180.0f, 180.0f) });
        return locView;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object def) {
        latlon = restoreValue ? getPersistedString(DEFAULT) : (String) def;
        persistString(latlon);
        setSummary(latlon == null ? "" : latlon);
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        if (id == Dialog.BUTTON_POSITIVE)
            setNewLocation(getLat(), getLon());
        else
            latlon = getPersistedString(DEFAULT);
    }

    public void setNewLocation(@NotNull Location location) {
        setNewLocation((float) location.getLatitude(), (float) location.getLongitude());
    }

    public void setNewLocation(float latitude, float longitude) {
        String formatted = String.format(Locale.US, fmt3, latitude, longitude);
        persistString(formatted);
        callChangeListener(formatted);
        setSummary(formatted);
    }
}
