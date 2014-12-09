package com.aragaer.jtt.resources;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.aragaer.jtt.Settings;
import com.aragaer.jtt.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class StringResources implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	public static final int TYPE_HOUR_NAME = 0x1;
	public static final int TYPE_TIME_FORMAT = 0x2;
	private int change_pending;

	private final BroadcastReceiver TZChangeReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				df = android.text.format.DateFormat.getTimeFormat(c);
				change_pending |= TYPE_TIME_FORMAT;
				notifyChange();
			}
		}
	};

	private static final IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
	private final Context c;
	private final Resources r;
	private String Hours[], HrOf[], Quarters[];
	private DateFormat df;
	private int hour_name_option;

	protected StringResources(final Context context) {
		c = context;
		r = new Resources(c.getAssets(), null, null);
		final SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(c);
		hour_name_option = Integer.parseInt(pref.getString(Settings.PREF_HNAME, "0"));
		setLocale(pref.getString(Settings.PREF_LOCALE, ""));
		pref.registerOnSharedPreferenceChangeListener(this);
	}

	private static void setLocaleToResources(final String l, final Resources r) {
		final Locale locale = l.length() == 0
				? Resources.getSystem().getConfiguration().locale
				: new Locale(l);
		final Configuration config = r.getConfiguration();
		config.locale = locale;
		r.updateConfiguration(config, null);
	}

	public static void setLocaleToContext(Context context) {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		final String lang = pref.getString(Settings.PREF_LOCALE, "");
		setLocaleToResources(lang, context.getResources());
	}

	private synchronized void setLocale(final String l) {
		setLocaleToResources(l, c.getApplicationContext().getResources());
		load_hour_names();
		df = android.text.format.DateFormat.getTimeFormat(c);
		change_pending |= TYPE_TIME_FORMAT;
	}

	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		if (key.equals(Settings.PREF_LOCALE))
			setLocale(pref.getString(key, ""));
		else if (key.equals(Settings.PREF_HNAME)) {
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

	public String getQuarter(final int q) {
		return Quarters[q];
	}

	public interface StringResourceChangeListener {
		public void onStringResourcesChanged(final int changes);
	}

	private final Map<StringResourceChangeListener, Integer> listeners = new HashMap<StringResources.StringResourceChangeListener, Integer>();

	public synchronized void registerStringResourceChangeListener(
			final StringResourceChangeListener listener, final int changeMask) {
		if (listeners.size() == 0)
			c.registerReceiver(TZChangeReceiver, filter);
		listeners.put(listener, changeMask);
	}

	public synchronized void unregisterStringResourceChangeListener(
			final StringResourceChangeListener listener) {
		listeners.remove(listener);
		if (listeners.size() == 0)
			c.unregisterReceiver(TZChangeReceiver);
	}

	private synchronized void notifyChange() {
		for (StringResourceChangeListener listener : listeners.keySet())
			if ((listeners.get(listener) & change_pending) != 0)
				listener.onStringResourcesChanged(change_pending);
		change_pending = 0;
	}

	private static final int hnh[] = { R.array.hour, R.array.romaji_hour, R.array.hiragana_hour };
	private static final int hnhof[] = { R.array.hour_of, R.array.romaji_hour_of, R.array.hiragana_hour_of };
	private static final int q[] = { R.array.quarter, R.array.romaji_quarter, R.array.hiragana_quarter };

	private void load_hour_names() {
		HrOf = r.getStringArray(hnhof[hour_name_option]);
		Hours = r.getStringArray(hnh[hour_name_option]);
		Quarters = r.getStringArray(q[hour_name_option]);
		change_pending |= TYPE_HOUR_NAME;
	}

	public String format_time(final long timestamp) {
		return df.format(timestamp);
	}
}