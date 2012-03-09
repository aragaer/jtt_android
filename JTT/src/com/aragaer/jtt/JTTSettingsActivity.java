package com.aragaer.jtt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
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
        ((Preference) findPreference("jtt_notify"))
                .setOnPreferenceChangeListener(changeListener);
        ((Preference) findPreference("jtt_stop")).setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return tryStop();
            }
        });
    }

    private void doSendMessage(int what, Bundle data) {
        ((JTTMainActivity) getParent()).send_msg_to_service(what, data);
    }

    private Boolean tryStop() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.stop_ask).setCancelable(true)
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stopService(new Intent(JTTService.class.getName()));
                ((JTTMainActivity) JTTSettingsActivity.this.getParent()).finish();
            }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();

        alert.show();
        return false;
    }
}
