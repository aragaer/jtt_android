package com.aragaer.jtt;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class Settings extends PreferenceActivity {
	public static final String PREF_LOCATION = "jtt_loc",
			PREF_LOCALE = "jtt_locale",
			PREF_HNAME = "jtt_hname",
			PREF_NOTIFY = "jtt_notify";
	public final static String JTT_SETTINGS_CHANGED = "com.aragaer.jtt.ACTION_JTT_SETTINGS";
	private final static String TAG = "JTT_SETTINGS";

	private JttService service = null;
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName cn, IBinder binder) {
			Log.d(TAG, "Connected to service");
			service = ((JttService.JttServiceBinder) binder).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName cn) {
			Log.d(TAG, "Disconnected from service");
			service = null;
		}
	};

	private static final String prefcodes[] = new String[] {PREF_LOCATION, PREF_NOTIFY, "jtt_widget_text_invert", PREF_LOCALE, "jtt_theme", PREF_HNAME};

	private final Map<String, Integer> listeners = new HashMap<String, Integer>();

	final OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Bundle b = new Bundle();
			Intent i = new Intent(JTT_SETTINGS_CHANGED);
			int code = listeners.get(preference.getKey());
			switch (code) {
			case 2:
				i.putExtra("inverse", (Boolean) newValue);
				break;
			case 3:
				i.putExtra("locale", (String) newValue);
				break;
			case 5:
				final ListPreference lp = (ListPreference) preference;
				i.putExtra("hname", (String) newValue);
				lp.setSummary(lp.getEntries()[lp.findIndexOfValue((String) newValue)]);
				break;
			default:
				break;
			}

			switch (code) {
			case 2:
			case 5:
				sendBroadcast(i, "com.aragaer.jtt.JTT_SETTINGS");
				break;
			case 3:
				sendBroadcast(i, "com.aragaer.jtt.JTT_SETTINGS");
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
		getApplicationContext().bindService(new Intent(this, JttService.class), connection, Context.BIND_AUTO_CREATE);

		addPreferencesFromResource(R.layout.preferences);
		ListPreference pref_locale = (ListPreference) findPreference(PREF_LOCALE);

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
				(new AlertDialog.Builder(Settings.this))
				.setTitle(R.string.stop)
				.setMessage(R.string.stop_ask)
				.setPositiveButton(android.R.string.yes, stop_dlg_listener)
				.setNegativeButton(android.R.string.no, stop_dlg_listener).show();
				return false;
			}
		});

		for (int i = 0; i < prefcodes.length; i++) {
			listeners.put(prefcodes[i], i);
			final Preference pref = (Preference) findPreference(prefcodes[i]);
			pref.setOnPreferenceChangeListener(listener);
			if (pref instanceof ListPreference) {
				final ListPreference lp = (ListPreference) pref;
				lp.setSummary(lp.getEntry());
			}
		}
	}

	private final OnClickListener stop_dlg_listener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
			switch (id) {
			case Dialog.BUTTON_POSITIVE:
				service.stopSelf();
				getParent().finish();
				break;
			case Dialog.BUTTON_NEGATIVE:
			default:
				dialog.cancel();
				break;
			}
		}
	};

	public static float[] getLocation(final Context context) {
		String[] ll = PreferenceManager
				.getDefaultSharedPreferences(context)
				.getString(Settings.PREF_LOCATION, LocationPreference.DEFAULT)
				.split(":");
		return new float[] { Float.parseFloat(ll[0]), Float.parseFloat(ll[1]) };
	}
}
