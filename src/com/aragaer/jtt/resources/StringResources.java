package com.aragaer.jtt.resources;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.aragaer.jtt.JTTSettingsActivity;
import com.aragaer.jtt.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class StringResources implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	public static final int TYPE_HOUR_NAME = 0x1;
	public static final int TYPE_TIME_FORMAT = 0x2;
	private int change_pending;

	private final Context c;
	private final Resources r;
	private String Hours[], HrOf[];
	private DateFormat df;
	private int hour_name_option;

	protected StringResources(final Context context) {
		c = context;
		r = new Resources(c.getAssets(), null, null);
		final SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(c);
		hour_name_option = Integer.parseInt(pref.getString(JTTSettingsActivity.PREF_HNAME, "0"));
		setLocale(pref.getString(JTTSettingsActivity.PREF_LOCALE, ""));
		pref.registerOnSharedPreferenceChangeListener(this);
	}

	private synchronized void setLocale(final String l) {
		final Locale locale = l.length() == 0
				? Resources.getSystem().getConfiguration().locale
				: new Locale(l);

		final Configuration config = r.getConfiguration();
		config.locale = locale;
		r.updateConfiguration(config, null);
		load_hour_names();
		df = android.text.format.DateFormat.getTimeFormat(c);
		change_pending |= TYPE_TIME_FORMAT;
	}

	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		if (key.equals(JTTSettingsActivity.PREF_LOCALE))
			setLocale(pref.getString(key, ""));
		else if (key.equals(JTTSettingsActivity.PREF_HNAME)) {
			hour_name_option = Integer.parseInt(pref.getString(key, "0"));
			load_hour_names();
		}
		notifyChange();
	}

	public String getHour(final int num) {
		return Hours[num];
	}

	public String getHrOf(final int num) {
		return HrOf[num];
	}

	public interface StringResourceChangeListener {
		public void onStringResourcesChanged(final int changes);
	}

	private final Map<StringResourceChangeListener, Integer> listeners = new HashMap<StringResources.StringResourceChangeListener, Integer>();

	public synchronized void registerStringResourceChangeListener(
			final StringResourceChangeListener listener, final int changeMask) {
		listeners.put(listener, changeMask);
	}

	public synchronized void unregisterStringResourceChangeListener(
			final StringResourceChangeListener listener) {
		listeners.remove(listener);
	}

	private synchronized void notifyChange() {
		for (StringResourceChangeListener listener : listeners.keySet())
			if ((listeners.get(listener) & change_pending) != 0)
				listener.onStringResourcesChanged(change_pending);
		change_pending = 0;
	}

	private static final int hnh[] = { R.array.hour, R.array.romaji_hour, R.array.hiragana_hour };
	private static final int hnhof[] = { R.array.hour_of, R.array.romaji_hour_of, R.array.hiragana_hour_of };

	private void load_hour_names() {
		HrOf = r.getStringArray(hnhof[hour_name_option]);
		Hours = r.getStringArray(hnh[hour_name_option]);
		change_pending |= TYPE_HOUR_NAME;
	}

	public String format_time(final long timestamp) {
		return df.format(timestamp);
	}
}
