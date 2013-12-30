package com.aragaer.jtt.resources;

import java.util.HashMap;
import java.util.Map;

import com.aragaer.jtt.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

public class ColorResources implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	public static final int TYPE_APP_THEME = 0x1;
	public static final int TYPE_WIDGET_STYLE = 0x2;
	private int change_pending;

	private final Context c;

	protected ColorResources(final Context context) {
		c = context;
		final SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(c);
		pref.registerOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
//		if (key.equals(Settings.PREF_THEME))
//			change_pending = TYPE_APP_THEME;
//		else if (key.equals(Settings.PREF_WIDGET_STYLE))
//			change_pending = TYPE_WIDGET_STYLE;
		notifyChange();
	}

	public interface ColorResourceChangeListener {
		public void onColorResourcesChanged(final int changes);
	}

	private final Map<ColorResourceChangeListener, Integer> listeners = new HashMap<ColorResourceChangeListener, Integer>();

	public synchronized void registerStringResourceChangeListener(
			final ColorResourceChangeListener listener, final int changeMask) {
		listeners.put(listener, changeMask);
	}

	public synchronized void unregisterStringResourceChangeListener(
			final ColorResourceChangeListener listener) {
		listeners.remove(listener);
	}

	private synchronized void notifyChange() {
		for (ColorResourceChangeListener listener : listeners.keySet())
			if ((listeners.get(listener) & change_pending) != 0)
				listener.onColorResourcesChanged(change_pending);
		change_pending = 0;
	}

	public int getGlyphStroke(boolean widget) {
		return Color.parseColor(c.getString(R.color.wadokei_stroke));
	}

	public int getWadokeiStroke(boolean widget) {
		return Color.parseColor(c.getString(R.color.wadokei_stroke));
	}

	public int getDayFill(boolean widget) {
		return Color.parseColor(c.getString(R.color.wadokei_fill));
	}

	public int getGlyphFill(boolean widget) {
		return Color.parseColor(c.getString(R.color.night_fill));
	}

	public int getNightFill(boolean widget) {
		return Color.parseColor(c.getString(R.color.night_fill));
	}

	public int getWadokeiFill(boolean widget) {
		return Color.parseColor(c.getString(R.color.wadokei_fill));
	}
}
