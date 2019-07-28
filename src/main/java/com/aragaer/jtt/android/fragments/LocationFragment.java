// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.android.fragments;

import com.aragaer.jtt.LocationPreference;
import com.aragaer.jtt.R;
import com.aragaer.jtt.Settings;
import com.aragaer.jtt.android.dialogs.WaitingForLocationDialog;
import com.aragaer.jtt.resources.StringResources;

import android.Manifest;
import android.app.ActionBar;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;


public class LocationFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private Preference.OnPreferenceChangeListener _listener;

    public void setChangeListener(Preference.OnPreferenceChangeListener listener) {
        _listener = listener;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StringResources.setLocaleToContext(getActivity());
        addPreferencesFromResource(R.xml.location);
        setHasOptionsMenu(true);
        if (_listener != null)
            findPreference(Settings.PREF_LOCATION).setOnPreferenceChangeListener(_listener);
    }

    @Override public void onStart() {
        super.onStart();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!settings.contains(Settings.PREF_LOCATION))
            Toast.makeText(getActivity(), "Please set location", Toast.LENGTH_LONG).show();
        Preference auto_button = findPreference("jtt_auto");
        auto_button.setOnPreferenceClickListener(this);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.location);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        }
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private boolean canUseLocation() {
        Log.d("JTT LOCATION", "Checking location service access");

        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (lm == null)
            return false;

        String provider = null;
        String permission = Manifest.permission.ACCESS_COARSE_LOCATION;

        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            permission = Manifest.permission.ACCESS_FINE_LOCATION;
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                provider = LocationManager.GPS_PROVIDER;
        }

        if (provider == null) {
            Log.d("JTT LOCATION", "No provider found");
            Toast.makeText(getActivity(), R.string.no_providers, Toast.LENGTH_SHORT).show();
            getActivity().startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d("JTT LOCATION", "Requesting permission for " + provider);
                requestPermissions(new String[]{permission}, 0);
                return false;
            } else
                Log.d("JTT LOCATION", "Permission granted");
        }
        return true;
    }

    private void getLocation() throws SecurityException {
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (lm == null)
            return;

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String provider = lm.getBestProvider(criteria, true);
        if (provider == null)
            return;

        Log.d("JTT LOCATION", "will use provider " + provider);

        Location last = lm.getLastKnownLocation(provider);

        LocationPreference pref = (LocationPreference) findPreference(Settings.PREF_LOCATION);
        if (last != null) {
            Log.d("JTT LOCATION", "Got last location, that's good enough");
            pref.setNewLocation(last);
            return;
        }

        WaitingForLocationDialog dialog = new WaitingForLocationDialog(getActivity());
        dialog.setPreference(pref);
        dialog.show();
        Log.d("JTT LOCATION", "Start requesting updates");
        lm.requestLocationUpdates(provider, 0, 0, dialog);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        Log.d("JTT LOCATION", "Got permission result: " + grantResults[0]);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            getLocation();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("jtt_auto")) {
            if (canUseLocation())
                getLocation();
            return true;
        }
        return false;
    }
}
