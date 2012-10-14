package com.aragaer.jtt;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public final class JTTUtil {
    public static Locale locale = null;
    static StringsHelper sh = null;

    public static final void initLocale(Context c) {
        changeLocale(c.getApplicationContext(), PreferenceManager
                .getDefaultSharedPreferences(c).getString("jtt_locale", ""));
    }

    public static final void changeLocale(Context c, String l) {
        if (l == null) // do not change anything
            return;
        locale = l.length() == 0
                ? Resources.getSystem().getConfiguration().locale
                : new Locale(l);

        final Resources r = c.getResources();
        final Configuration conf = r.getConfiguration();
        conf.locale = locale;
        r.updateConfiguration(conf, null);
        if (sh != null)
            sh.load(c);
    }

    public static final StringsHelper getStringsHelper(Context ctx) {
        if (sh == null)
            sh = new StringsHelper(ctx);
        return sh;
    }

    public static class StringsHelper {
        private String Hours[], HrOf[];
        private HashMap<String, Integer> H2N = new HashMap<String, Integer>(12);

        public String getHour(int num) {
            return Hours[num];
        }

        public String getHrOf(int num) {
            return HrOf[num];
        }

        private StringsHelper(Context ctx) {
            load(ctx);
        }

        private void load(Context ctx) {
            Resources r = ctx.getApplicationContext().getResources();
            HrOf = r.getStringArray(R.array.hour_of);
            Hours = r.getStringArray(R.array.hour);
            for (int i = 0; i < 12; i++)
                H2N.put(Hours[i], i);
        }

        public int name_to_num(String name) {
            return H2N.get(name);
        }

        public JTTHour makeHour(String hour) {
            return makeHour(hour, 0);
        }

        public JTTHour makeHour(String hour, int fraction) {
            return new JTTHour(name_to_num(hour), fraction);
        }
    }
}
