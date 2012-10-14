package com.aragaer.jtt;

import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public final class JTTUtil {
    public static final Locale def_locale = Resources.getSystem().getConfiguration().locale;
    public static Locale locale = def_locale;

    public static final void setLocale(Context c) {
        setLocale(c.getApplicationContext(), PreferenceManager
                .getDefaultSharedPreferences(c).getString("jtt_locale", ""));
    }

    public static final void setLocale(Context c, String l) {
        if (l == null) // do not change anything
            return;
        locale = l.length() == 0 ? def_locale : new Locale(l);

        final Resources r = c.getResources();
        final Configuration conf = r.getConfiguration();
        conf.locale = locale;
        r.updateConfiguration(conf, null);
    }
}
