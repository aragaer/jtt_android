// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.ui.android;

import com.aragaer.jtt.LocationPreference;

import android.text.Editable;
import android.text.TextWatcher;


public class LocationTextWatcher implements TextWatcher {
    private final static String fmt2 = "%s:%s";
    private final LocationPreference pref;

    public LocationTextWatcher(LocationPreference pref) {
        this.pref = pref;
    }

    @Override public void afterTextChanged(Editable arg0) {
        CharSequence latt = pref.getLatText();
        CharSequence lont = pref.getLonText();
        if (latt != null && lont != null)
            pref.doSet(String.format(fmt2, latt, lont));
    }

    @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    @Override public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }
}
