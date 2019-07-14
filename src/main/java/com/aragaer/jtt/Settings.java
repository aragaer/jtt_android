// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import android.content.Context;
import android.preference.PreferenceManager;


public class Settings {
    public static final String PREF_LOCATION = "jtt_loc";
    public static final String PREF_LOCALE = "jtt_locale";
    public static final String PREF_HNAME = "jtt_hname";
    public static final String PREF_NOTIFY = "jtt_notify";
    /* package private */ static final String PREF_THEME = "jtt_theme";
    /* package private */ static final String PREF_WIDGET = "jtt_widget_theme";
    public static final String PREF_EMOJI_WIDGET = "jtt_emoji_widget";

    private static final int[] app_themes = {R.style.JTTTheme, R.style.DarkTheme, R.style.LightTheme};
    /* package private */ static int getAppTheme(final Context  context) {
        String theme = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(PREF_THEME, context.getString(R.string.theme_default));
        try {
            return app_themes[Integer.parseInt(theme)];
        } catch (NumberFormatException e) {
            return app_themes[0];
        }
    }

    private static final int[] widget_themes = {R.style.JTTTheme, R.style.SolidDark, R.style.SolidLight};
    /* package private */ static int getWidgetTheme(final Context context) {
        String theme = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_WIDGET, context.getString(R.string.theme_default));
        try {
            return widget_themes[Integer.parseInt(theme)];
        } catch (NumberFormatException e) {
            return widget_themes[0];
        }
    }
}
