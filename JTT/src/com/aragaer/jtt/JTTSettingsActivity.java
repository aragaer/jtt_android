package com.aragaer.jtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class JTTSettingsActivity extends PreferenceActivity {
    private Preference prefLocation;
    private SharedPreferences settings;
    private LocationManager lm;
    private LocationListener ll;
    private float accuracy = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);
        settings = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        prefLocation = (Preference) findPreference("jtt_auto_loc");
        final String strlat = settings.getString("jtt_lat", "0.0");
        final String strlon = settings.getString("jtt_lon", "0.0");

        Boolean auto = settings.getBoolean("auto_location", false);
        if (auto)
            prefLocation
                    .setSummary("Current location " + strlat + ":" + strlon);
        Preference pref;
        pref = (Preference) findPreference("jtt_lat");
        pref.setEnabled(!auto);
        pref.setSummary(strlat);

        pref = (Preference) findPreference("jtt_lon");
        pref.setEnabled(!auto);
        pref.setSummary(strlon);

        OnPreferenceChangeListener changeListener = new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference pref, Object newVal) {
                if (pref.equals(prefLocation))
                    return prefLocationChange(newVal);
                return true;
            }
        };

        prefLocation.setOnPreferenceChangeListener(changeListener);
    }

    private boolean prefLocationChange(Object newValue) {
        final Boolean auto = (Boolean) newValue;
        Preference pref;
        pref = (Preference) findPreference("jtt_lat");
        pref.setEnabled(!auto);
        pref.setSummary(settings.getString("jtt_lat", "0.0"));

        pref = (Preference) findPreference("jtt_lon");
        pref.setEnabled(!auto);
        pref.setSummary(settings.getString("jtt_lon", "0.0"));
        if (!auto)
            return true;

        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        ll = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location
                // provider.
                makeUseOfNewLocation(location, true);
            }

            public void onStatusChanged(String provider, int status,
                    Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        Location last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (last == null)
            last = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (last != null)
            makeUseOfNewLocation(last, false);

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);

        return true;
    }

    private void makeUseOfNewLocation(Location l, Boolean stopLocating) {
        if (l.hasAccuracy()) {
            final float new_acc = l.getAccuracy();
            if (accuracy > 0 && accuracy < new_acc)
                return; // this one doesn't have the best accuracy
            accuracy = new_acc;
        }
        final String lat = Double.toString(l.getLatitude());
        final String lon = Double.toString(l.getLongitude());
//        Toast.makeText(getBaseContext(), "Current location " + lat + ":" + lon,
//                Toast.LENGTH_SHORT).show();
        prefLocation.setSummary("Current location " + lat + ":" + lon);
        final SharedPreferences.Editor editor1 = settings.edit();
        editor1.putString("jtt_lat", "" + lat);
        editor1.putString("jtt_lon", "" + lon);
        editor1.commit();

        if (stopLocating)
            lm.removeUpdates(ll);
    }
}
