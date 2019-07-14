// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import com.aragaer.jtt.resources.StringResources;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.view.Menu;
import android.view.MenuInflater;


public class LocationFragment extends PreferenceFragment {

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StringResources.setLocaleToContext(getActivity());
        addPreferencesFromResource(R.xml.location);
        setHasOptionsMenu(true);
    }

    @Override public void onStart() {
        super.onStart();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!settings.contains(Settings.PREF_LOCATION)) // location is not set
            ((LocationPreference) findPreference(Settings.PREF_LOCATION)).showDialog(null);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.location);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        }
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }
}
