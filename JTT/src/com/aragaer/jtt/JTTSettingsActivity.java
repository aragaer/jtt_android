package com.aragaer.jtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class JTTSettingsActivity extends PreferenceActivity {
	private Preference prefLocation;
	private SharedPreferences settings;
	private LocationManager locationManager;
	private LocationListener locationListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
		settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		prefLocation = (Preference) findPreference("auto_location");

		Boolean auto = settings.getBoolean("auto_location", false);
		if (auto)
			prefLocation.setSummary("Current location "+settings.getString("posLat", "0.0")+":"+settings.getString("posLong", "0.0"));
		Preference pref;
		pref = (Preference) findPreference("posLat");
		pref.setEnabled(!auto);
		pref.setSummary(settings.getString("posLat", "0.0"));
		
		pref = (Preference) findPreference("posLong");
		pref.setEnabled(!auto);
		pref.setSummary(settings.getString("posLong", "0.0"));

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
		Boolean auto = (Boolean) newValue;
		Preference pref;
		if (!auto) {
			pref = (Preference) findPreference("posLat");
			pref.setEnabled(true);
			pref.setSummary(settings.getString("posLat", "0.0"));
			
			pref = (Preference) findPreference("posLong");
			pref.setEnabled(true);
			pref.setSummary(settings.getString("posLong", "0.0"));
			return true;
		}

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		locationListener = new LocationListener() {
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

		Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (last == null) {
			last = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		
		if (last != null) {
			makeUseOfNewLocation(last, false);
		}

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
						
		return true;
	}
	
	private void makeUseOfNewLocation(Location l, Boolean stopLocating) {
		Toast.makeText(getBaseContext(), "Current location "+l.getLatitude()+":"+l.getLongitude(),
				Toast.LENGTH_SHORT).show();
		prefLocation.setSummary("Current location "+l.getLatitude()+":"+l.getLongitude());
		SharedPreferences.Editor editor1 = settings.edit();
		editor1.putString("posLat", ""+l.getLatitude());
		editor1.putString("posLong", ""+l.getLongitude());
		editor1.commit();
		
		if (stopLocating)
			locationManager.removeUpdates(locationListener);
	}
}