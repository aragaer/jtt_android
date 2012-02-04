package com.aragaer.jtt;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class JTTSettingsActivity extends PreferenceActivity {
    private Preference prefLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);
        prefLocation = (Preference) findPreference("jtt_loc");

        OnPreferenceChangeListener changeListener = new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference pref, Object newVal) {
                Log.d("pref", pref.getKey()+" changed to "+newVal.toString());
                return true;
            }
        };

        prefLocation.setOnPreferenceChangeListener(changeListener);
    }
}
