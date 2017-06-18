// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import android.content.Context;
import android.preference.PreferenceManager;

import com.aragaer.jtt.astronomy.AndroidLocationHandler;


public class Settings {
    public static final String PREF_LOCATION = "jtt_loc",
        PREF_LOCALE = "jtt_locale",
        PREF_HNAME = "jtt_hname",
        PREF_NOTIFY = "jtt_notify",
        PREF_THEME = "jtt_theme",
        PREF_WIDGET = "jtt_widget_theme",
        PREF_EMOJI_WIDGET = "jtt_emoji_widget";

    public static float[] getLocation(final Context context) {
        return new AndroidLocationHandler(context).getLocation();
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

    static final int widget_themes[] = {R.style.JTTTheme, R.style.SolidDark, R.style.SolidLight};
    public static final int getWidgetTheme(final Context context) {
        String theme = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_WIDGET, context.getString(R.string.theme_default));
        try {
            return widget_themes[Integer.parseInt(theme)];
        } catch (NumberFormatException e) {
            return widget_themes[0];
        }
    }
}
