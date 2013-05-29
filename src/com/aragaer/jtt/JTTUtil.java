package com.aragaer.jtt;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

public final class JTTUtil {
	/* sets a value to a text field */
	final static void t(View v, int id, String t) {
		((TextView) v.findViewById(id)).setText(t);
	}

	static final int themes[] = {R.style.JTTTheme, R.style.DarkTheme};
	public static final void setTheme(Context c) {
		String theme = PreferenceManager.getDefaultSharedPreferences(c).getString("jtt_theme", c.getString(R.string.theme_default));
		c.setTheme(themes[Integer.parseInt(theme)]);
	}
}
