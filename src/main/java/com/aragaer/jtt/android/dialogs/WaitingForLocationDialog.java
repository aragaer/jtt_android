package com.aragaer.jtt.android.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.aragaer.jtt.LocationPreference;

public class WaitingForLocationDialog extends ProgressDialog implements LocationListener, DialogInterface.OnDismissListener {

    private LocationPreference _pref;

    public WaitingForLocationDialog(Context context) {
        super(context);
        setMessage("Getting the location");
        setIndeterminate(true);
        setCancelable(true);
        setProgressStyle(STYLE_SPINNER);
        setOnDismissListener(this);
    }

    public void setPreference(LocationPreference pref) {
        _pref = pref;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("JTT LOCATION", "Location changed");
        _pref.setNewLocation(location);
        dismiss();
    }

    @Override public void onStatusChanged(String s, int i, Bundle bundle) {}
    @Override public void onProviderEnabled(String s) {}
    @Override public void onProviderDisabled(String s) {
        dismiss();
    }

    @Override public void onDismiss(DialogInterface dialogInterface) {
        Log.d("JTT LOCATION", "Stop getting location");
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (lm != null)
            lm.removeUpdates(this);
    }
}
