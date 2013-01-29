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
	public static final String PREF_LOCATION = "com.aragaer.jtt.location";
	public static final String PREF_NOTIFY = "com.aragaer.jtt.notify";
	public static final String PREF_LOCALE = "com.aragaer.jtt.locale";
	public static final String PREF_INVERT = "com.aragaer.jtt.inverse";
	public final static String JTT_SETTINGS_CHANGED = "com.aragaer.jtt.ACTION_JTT_SETTINGS";
	private final static String TAG = "jtt settings";

	private static final String prefcodes[] = new String[] {"jtt_loc", "jtt_notify", "jtt_widget_text_invert", "jtt_locale", "jtt_theme"};
	private final Map<String, Integer> listeners = new HashMap<String, Integer>();

	final OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Intent i = new Intent(JTT_SETTINGS_CHANGED);
			int code = listeners.get(preference.getKey());
			switch (code) {
			case 0:
				i.putExtra(PREF_LOCATION, (String) newValue);
				break;
			case 1:
				i.putExtra(PREF_NOTIFY, (Boolean) newValue);
				break;
			case 2:
				i.putExtra(PREF_INVERT, (Boolean) newValue);
				break;
			case 3:
				i.putExtra(PREF_LOCALE, (String) newValue);
			default:
				break;
			}

			switch (code) {
			case 0:
			case 1:
				/* notify service only */
				startService(i);
				break;
			case 2:
				/* notify widgets only */
				sendBroadcast(i, "com.aragaer.jtt.JTT_SETTINGS");
				break;
			case 3:
				/* notify both service and widgets */
				startService(i);
				sendBroadcast(i, "com.aragaer.jtt.JTT_SETTINGS");
				JTTUtil.changeLocale(getApplicationContext(), (String) newValue);
				/* fall-through */
			case 4:
				i = getParent().getIntent();
				i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				getParent().finish();
				startActivity(i);
				break;
			default:
				break;
			}
			return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.layout.preferences);
		ListPreference pref_locale = (ListPreference) findPreference("jtt_locale");

		Log.d(TAG, "settings created");

		for (int i = 0; i < prefcodes.length; i++) {
			listeners.put(prefcodes[i], i);
			((Preference) findPreference(prefcodes[i])).setOnPreferenceChangeListener(listener);
		}

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

	private final OnClickListener stop_dlg_listener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
			switch (id) {
			case Dialog.BUTTON_POSITIVE:
				startService(new Intent(JTTService.STOP_ACTION));
				getParent().finish();
				break;
			case Dialog.BUTTON_NEGATIVE:
			default:
				dialog.cancel();
				break;
			}
		}
	};
}
