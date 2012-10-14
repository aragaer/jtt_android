package com.aragaer.jtt;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class JTTSettingsActivity extends PreferenceActivity {
    public final static String JTT_SETTINGS_CHANGED = "com.aragaer.jtt.ACTION_JTT_SETTINGS";
    private final static String TAG = "jtt settings";

    private Map<String, OnPreferenceChangeListener> listeners = new HashMap<String, Preference.OnPreferenceChangeListener>() {
        private static final long serialVersionUID = 1L;
        {
            put("jtt_loc", new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Bundle b = new Bundle();
                    b.putString("latlon", (String) newValue);
                    doSendMessage(JTTService.MSG_UPDATE_LOCATION, b);
                    return true;
                }
            });
            put("jtt_notify", new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Bundle b = new Bundle();
                    b.putBoolean("notify", (Boolean) newValue);
                    doSendMessage(JTTService.MSG_SETTINGS_CHANGE, b);
                    return true;
                }
            });
            put("jtt_widget_text_invert", new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Intent i = new Intent(JTT_SETTINGS_CHANGED);
                    i.putExtra("inverse", (Boolean) newValue);
                    sendBroadcast(i, "com.aragaer.jtt.JTT_SETTINGS");
                    return true;
                }
            });
            put("jtt_locale", new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Intent i = new Intent(JTT_SETTINGS_CHANGED);
                    i.putExtra("locale", (String) newValue);
                    sendBroadcast(i, "com.aragaer.jtt.JTT_SETTINGS");
                    Bundle b = new Bundle();
                    b.putString("locale", (String) newValue);
                    doSendMessage(JTTService.MSG_SETTINGS_CHANGE, b);
                    JTTUtil.changeLocale(getApplicationContext(), (String) newValue);
                    i = getParent().getIntent();
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    getParent().finish();
                    startActivity(i);
                    return true;
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.layout.preferences);
        ListPreference pref_locale = (ListPreference) findPreference("jtt_locale");

        Log.d(TAG, "settings created");

        for (String p : listeners.keySet())
            ((Preference) findPreference(p)).setOnPreferenceChangeListener(listeners.get(p));

        final CharSequence[] llist = pref_locale.getEntryValues();
        final CharSequence[] lnames = new CharSequence[llist.length];
        lnames[0] = getString(R.string.locale_default);
        for (int i = 1; i < llist.length; i++) {
            final Locale l = new Locale(llist[i].toString());
            lnames[i] = l.getDisplayLanguage(l);
        }
        pref_locale.setEntries(lnames);

        ((Preference) findPreference("jtt_stop")).setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return tryStop();
            }
        });
    }

    private final void doSendMessage(int what, Bundle data) {
        ((JTTMainActivity) getParent()).send_msg_to_service(what, data);
    }

    private boolean tryStop() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.stop_ask).setCancelable(true).setTitle(R.string.stop)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                doSendMessage(JTTService.MSG_STOP, null);

                ((JTTMainActivity) getParent()).finish();
            }
        })
        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();

        alert.show();
        return false;
    }
}
