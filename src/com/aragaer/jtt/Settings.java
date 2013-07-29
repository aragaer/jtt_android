package com.aragaer.jtt;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener, DialogInterface.OnClickListener {
	public static final String PREF_LOCATION = "jtt_loc",
			PREF_LOCALE = "jtt_locale",
			PREF_HNAME = "jtt_hname",
			PREF_NOTIFY = "jtt_notify",
			PREF_WIDGET_INVERSE = "jtt_widget_text_invert";

	private static final String prefcodes[] = new String[] {PREF_LOCATION, PREF_NOTIFY, PREF_WIDGET_INVERSE, PREF_LOCALE, "jtt_theme", PREF_HNAME};

	private final Map<String, Integer> listeners = new HashMap<String, Integer>();

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (listeners.get(preference.getKey())) {
		case 3:
		case 4:
			Intent i = getParent().getIntent();
			i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			getParent().finish();
			startActivity(i);
			break;
		case 5:
			final ListPreference lp = (ListPreference) preference;
			lp.setSummary(lp.getEntries()[lp.findIndexOfValue((String) newValue)]);
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		((Preference) findPreference("jtt_stop")).setOnPreferenceClickListener(this);

		for (int i = 0; i < prefcodes.length; i++) {
			listeners.put(prefcodes[i], i);
			final Preference pref = (Preference) findPreference(prefcodes[i]);
			pref.setOnPreferenceChangeListener(this);
			if (pref instanceof ListPreference) {
				final ListPreference lp = (ListPreference) pref;
				lp.setSummary(lp.getEntry());
			}
		}
	}

	public boolean onPreferenceClick(Preference preference) {
		(new AlertDialog.Builder(this))
		.setTitle(R.string.stop)
		.setMessage(R.string.stop_ask)
		.setPositiveButton(android.R.string.yes, this)
		.setNegativeButton(android.R.string.no, this).show();
		return false;
	}

	public void onClick(DialogInterface dialog, int id) {
		if (id == Dialog.BUTTON_POSITIVE) {
			startService(new Intent(this, JttService.class).setAction(JttService.STOP_ACTION));
			getParent().finish();
		} else
			dialog.cancel();
	}

	public static float[] getLocation(final Context context) {
		String[] ll = PreferenceManager
				.getDefaultSharedPreferences(context)
				.getString(PREF_LOCATION, LocationPreference.DEFAULT)
				.split(":");
		try {
			return new float[] { Float.parseFloat(ll[0]), Float.parseFloat(ll[1]) };
		} catch (NumberFormatException e) {
			return new float[] { 0, 0 };
		}
	}

	static final int themes[] = {R.style.JTTTheme, R.style.DarkTheme};
	public static final int getTheme(final Context context) {
		String theme = PreferenceManager
				.getDefaultSharedPreferences(context)
				.getString("jtt_theme", context.getString(R.string.theme_default));
		try {
			return themes[Integer.parseInt(theme)];
		} catch (NumberFormatException e) {
			return themes[0];
		}
	}
}
