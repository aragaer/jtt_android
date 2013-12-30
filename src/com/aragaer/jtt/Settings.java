package com.aragaer.jtt;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity implements OnPreferenceChangeListener {
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
			finish();
			break;
		case 4:
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
		setContent();
	}

	private void setContent() {
		StringResources.setLocaleToContext(this);
		addPreferencesFromResource(R.layout.preferences);
		ListPreference pref_locale = (ListPreference) findPreference(PREF_LOCALE);

		final CharSequence[] llist = pref_locale.getEntryValues();
		final CharSequence[] lnames = new CharSequence[llist.length];
		lnames[0] = getString(R.string.locale_default);
		for (int i = 1; i < llist.length; i++) {
			final Locale l = new Locale(llist[i].toString());
			final String name = l.getDisplayLanguage(l);
			lnames[i] = name.substring(0, 1).toUpperCase(l) + name.substring(1);
		}
		pref_locale.setEntries(lnames);

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

	// Unfortunately I can't (yet?) directly control configuration changes in this activity
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContent();
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

	@Override
	protected void onStart() {
		super.onStart();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (!settings.contains("jtt_loc")) // location is not set
			((LocationPreference) findPreference("jtt_loc")).showMe();
	}
}
