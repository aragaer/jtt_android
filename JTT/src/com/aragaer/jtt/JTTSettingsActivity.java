package com.aragaer.jtt;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class JTTSettingsActivity extends PreferenceActivity {
    private Preference prefLocation;

    private final static String TAG = "jtt settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.layout.preferences);
        prefLocation = (Preference) findPreference("jtt_loc");

        Log.d(TAG, "settings created");

        OnPreferenceChangeListener changeListener = new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference pref, Object newVal) {
                if (pref.equals(prefLocation)) {
                    Bundle b = new Bundle();
                    b.putString("latlon", (String) newVal);
                    doSendMessage(JTTService.MSG_UPDATE_LOCATION, b);
                } else if (pref.getKey().equals("jtt_notify")) {
                    Bundle b = new Bundle();
                    b.putBoolean("notify", (Boolean) newVal);
                    doSendMessage(JTTService.MSG_TOGGLE_NOTIFY, b);
                }
                return true;
            }
        };

        prefLocation.setOnPreferenceChangeListener(changeListener);
        ((Preference) findPreference("jtt_notify")).setOnPreferenceChangeListener(changeListener);
    }

    private void doSendMessage(int what, Bundle data) {
        ((JTTMainActivity) getParent()).send_msg_to_service(what, data);
    }
}
