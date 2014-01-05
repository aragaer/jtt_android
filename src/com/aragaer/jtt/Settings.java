package com.aragaer.jtt;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.aragaer.jtt.resources.StringResources;

import android.content.Context;
import android.content.SharedPreferences;
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
			PREF_THEME = "jtt_theme",
			PREF_WIDGET = "jtt_widget_theme";

	private static final String prefcodes[] = new String[] {PREF_LOCATION, PREF_NOTIFY, PREF_LOCALE, PREF_HNAME, PREF_THEME, PREF_WIDGET};

	private final Map<String, Integer> listeners = new HashMap<String, Integer>();

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference instanceof ListPreference) {
			final ListPreference lp = (ListPreference) preference;
			lp.setSummary(lp.getEntries()[lp.findIndexOfValue((String) newValue)]);
		}

		switch (listeners.get(preference.getKey())) {
		case 2:
			finish(); // Main activity will restart us
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

	@Override
	public void onRestoreInstanceState(Bundle state) {
		StringResources.setLocaleToContext(this);
		super.onRestoreInstanceState(state);
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

	static final int app_themes[] = {R.style.JTTTheme, R.style.DarkTheme, R.style.LightTheme};
	public static final int getAppTheme(final Context context) {
		String theme = PreferenceManager
				.getDefaultSharedPreferences(context)
				.getString(PREF_THEME, context.getString(R.string.theme_default));
		try {
			return app_themes[Integer.parseInt(theme)];
		} catch (NumberFormatException e) {
			return app_themes[0];
		}
	}

	static final int widget_themes[] = {R.style.JTTTheme, R.style.SolidLight};
	public static final int getWidgetTheme(final Context context) {
		String theme = PreferenceManager
				.getDefaultSharedPreferences(context)
				.getString(PREF_WIDGET, context.getString(R.string.theme_default));
		try {
			return widget_themes[Integer.parseInt(theme)];
		} catch (NumberFormatException e) {
			return widget_themes[0];
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (!settings.contains("jtt_loc")) // location is not set
			((LocationPreference) findPreference("jtt_loc")).showDialog(null);
	}
}
