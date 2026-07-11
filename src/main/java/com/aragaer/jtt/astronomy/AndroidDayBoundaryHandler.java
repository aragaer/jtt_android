// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aragaer.jtt.Settings;


public class AndroidDayBoundaryHandler implements DayBoundaryHandler {

    /* Official sunrise/sunset: sun 50' below the horizon. */
    private static final double OFFICIAL = 0.8333;
    /* Civil twilight: sun 6 degrees below the horizon. */
    private static final double CIVIL = 6;
    /* Edo-period convention for ake-mutsu/kure-mutsu: sun 7 degrees
     * 21' 40" below the horizon.  Before the Kansei reform, dawn and
     * dusk were fixed at 2.5 koku (= 36 min; 1 day = 100 koku) before
     * sunrise / after sunset; the Kansei calendar (1798-1843) redefined
     * them as the solar depression matching that offset at the equinox
     * in Kyoto: sin h = -cos(lat) * sin(9 deg), since 2.5 koku = 9
     * degrees of rotation, giving h = -7deg21'41".  The NAOJ ephemeris
     * still defines yoake/higure by this angle.  Source: NAOJ Koyomi
     * Wiki, "Twilight: yoake to higure",
     * https://eco.mtk.nao.ac.jp/koyomi/wiki/C7F6CCC02FCCEBCCC0A4C8C6FCCAEB.html */
    private static final double EDO = 7.3611;

    private static final double[] DEPRESSIONS = {OFFICIAL, CIVIL, EDO};

    private final SharedPreferences _pref;

    /* package private */ AndroidDayBoundaryHandler(Context context) {
        _pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public double getDepression() {
        String boundary = _pref.getString(Settings.PREF_BOUNDARY, "0");
        try {
            return DEPRESSIONS[Integer.parseInt(boundary)];
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return OFFICIAL;
        }
    }
}
