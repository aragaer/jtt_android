package com.aragaer.jtt;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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

    private static abstract class PreferenceBroadcast {
        abstract public void process(Object value);
    }

    private final Map<String, PreferenceBroadcast> listeners = new HashMap<String, PreferenceBroadcast>() {
        private static final long serialVersionUID = 1L;
        {
            put("jtt_loc", new PreferenceBroadcast() {
                public void process(Object value) {
                    Bundle b = new Bundle();
                    b.putString("latlon", (String) value);
                    doSendMessage(JTTService.MSG_UPDATE_LOCATION, b);
                }
            });
            put("jtt_notify", new PreferenceBroadcast() {
                public void process(Object value) {
                    Bundle b = new Bundle();
                    b.putBoolean("notify", (Boolean) value);
                    doSendMessage(JTTService.MSG_SETTINGS_CHANGE, b);
                }
            });
            put("jtt_widget_text_invert", new PreferenceBroadcast() {
                public void process(Object value) {
                    Intent i = new Intent(JTT_SETTINGS_CHANGED);
                    i.putExtra("inverse", (Boolean) value);
                    sendBroadcast(i, "com.aragaer.jtt.JTT_SETTINGS");
                }
            });
            put("jtt_locale", new PreferenceBroadcast() {
                public void process(Object value) {
                    Intent i = new Intent(JTT_SETTINGS_CHANGED);
                    i.putExtra("locale", (String) value);
                    sendBroadcast(i, "com.aragaer.jtt.JTT_SETTINGS");
                    Bundle b = new Bundle();
                    b.putString("locale", (String) value);
                    doSendMessage(JTTService.MSG_SETTINGS_CHANGE, b);
                    JTTUtil.changeLocale(getApplicationContext(), (String) value);
                    i = getParent().getIntent();
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    getParent().finish();
                    startActivity(i);
                }
            });
        }
    };

    final OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            listeners.get(preference.getKey()).process(newValue);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.layout.preferences);
        ListPreference pref_locale = (ListPreference) findPreference("jtt_locale");

        Log.d(TAG, "settings created");

        for (String p : listeners.keySet())
            ((Preference) findPreference(p)).setOnPreferenceChangeListener(listener);

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
                (new AlertDialog.Builder(JTTSettingsActivity.this))
                    .setTitle(R.string.stop)
                    .setMessage(R.string.stop_ask)
                    .setPositiveButton(android.R.string.yes, stop_dlg_listener)
                    .setNegativeButton(android.R.string.no, stop_dlg_listener).show();
                return false;
            }
        });
    }

    private final void doSendMessage(int what, Bundle data) {
        ((JTTMainActivity) getParent()).send_msg_to_service(what, data);
    }

    private final OnClickListener stop_dlg_listener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            switch (id) {
            case Dialog.BUTTON_POSITIVE:
                doSendMessage(JTTService.MSG_STOP, null);
                ((JTTMainActivity) getParent()).finish();
                break;
            case Dialog.BUTTON_NEGATIVE:
            default:
                dialog.cancel();
                break;
            }
        }
    };
}
