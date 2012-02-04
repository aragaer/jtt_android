package com.aragaer.jtt;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;

public class LocationPreference extends DialogPreference implements
        LocationListener, TextWatcher {
    private float accuracy = 0;
    private LocationManager lm;
    private TextView lat, lon;
    private LinearLayout locView;
    private String latlon;

    private final static String fmt1 = "%.2f";
    private final static String fmt2 = "%s:%s";
    private final static String fmt3 = "%.2f:%.2f";

    public LocationPreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public LocationPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        locView = (LinearLayout) li.inflate(R.layout.location_picker, null);
        ((Button) locView.findViewById(R.id.auto_loc))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        acquireLocation();
                    }
                });

        lat = (TextView) locView.findViewById(R.id.lat);
        lon = (TextView) locView.findViewById(R.id.lon);

        if (latlon == null)
            latlon = getPersistedString("0.0:0.0");
        String[] ll = latlon.split(":");
        lat.setText(ll[0]);
        lon.setText(ll[1]);

        lat.addTextChangedListener(this);
        lon.addTextChangedListener(this);
        return locView;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object def) {
        if (restoreValue) {
            latlon = getPersistedString("0.0:0.0");
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

    private void doSet(String l) {
        latlon = l;
        persistString(latlon);
        callChangeListener(new String(latlon));
        setSummary(latlon);
    }

    private void makeUseOfNewLocation(Location l, Boolean stopLocating) {
        if (l.hasAccuracy()) {
            final float new_acc = l.getAccuracy();
            if (accuracy > 0 && accuracy < new_acc)
                return; // this one doesn't have the best accuracy
            accuracy = new_acc;
        }

        lat.setText(String.format(fmt1, l.getLatitude()));
        lon.setText(String.format(fmt1, l.getLongitude()));
        doSet(String.format(fmt3, l.getLatitude(), l.getLongitude()));

        if (stopLocating)
            lm.removeUpdates(this);
    }

    private void acquireLocation() {
        lm = (LocationManager) getContext().getSystemService(
                Context.LOCATION_SERVICE);

        Location last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (last != null)
            makeUseOfNewLocation(last, false);

        last = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (last != null)
            makeUseOfNewLocation(last, false);

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    public void onLocationChanged(Location location) {
        makeUseOfNewLocation(location, true);
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void afterTextChanged(Editable arg0) {
        CharSequence latt = lat.getText();
        CharSequence lont = lon.getText();
        if (latt != null && lont != null)
            doSet(String.format(fmt2, latt, lont));
    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
            int arg3) {
    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }
}
