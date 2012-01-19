package com.aragaer.jtt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class JTTSettingsActivity extends PreferenceActivity {
    private SharedPreferences settings;
    private LocationManager lm;
    private LocationListener ll;
    private float accuracy = 0;
    static final int MSG_TOGGLE_NOTIFY = 0;
    private Messenger srv;
    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("settings", "Service connection established");
            srv = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.i("settings", "Service connection closed");
            srv = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName("JTT");
        prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);
        
        addPreferencesFromResource(R.layout.preferences);
        settings = prefMgr.getSharedPreferences();
        final String strlat = settings.getString("jtt_lat", "0.0");
        final String strlon = settings.getString("jtt_lon", "0.0");

        Preference pref;
        pref = (Preference) findPreference("jtt_lat");
        pref.setSummary(strlat);

        pref = (Preference) findPreference("jtt_lon");
        pref.setSummary(strlon);

        OnPreferenceChangeListener changeListener = new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference pref, Object newVal) {
                Log.d("settings", "Pref "+pref.getKey()+" changed to "+newVal.toString());
                if (pref.getKey().equals("jtt_notify")) {
                    Log.d("settings", "yo! message!");
                    Message msg = Message.obtain(null, MSG_TOGGLE_NOTIFY);
                    try {
                        srv.send(msg);
                    } catch (Exception e) {
                        Log.e("settings", "error toggling notification", e);
                    }
                }
                return true;
            }
        };
        
        final Intent service = new Intent(getBaseContext(), JTTMainActivity.class);
        getBaseContext().startService(service);
        getBaseContext().bindService(service, conn, 0);
        
        ((Preference) findPreference("jtt_notify")).setOnPreferenceChangeListener(changeListener);
    }

    private void getloc() {
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
