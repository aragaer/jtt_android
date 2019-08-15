// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import java.util.*;

import com.aragaer.jtt.android.fragments.LocationFragment;
import com.aragaer.jtt.resources.StringResources;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.Menu;
import android.view.MenuInflater;


public class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {
    private static final String[] prefcodes = {Settings.PREF_LOCATION, Settings.PREF_NOTIFY,
            Settings.PREF_LOCALE, Settings.PREF_HNAME,
            Settings.PREF_THEME, Settings.PREF_WIDGET,
            Settings.PREF_EMOJI_WIDGET};

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            final ListPreference lp = (ListPreference) preference;
            lp.setSummary(lp.getEntries()[lp.findIndexOfValue((String) newValue)]);
        } else if (preference.getKey().equals(Settings.PREF_LOCATION)) {
            findPreference(Settings.PREF_LOCATION).setSummary((String) newValue);
        }
        return true;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StringResources.setLocaleToContext(getActivity());
        addPreferencesFromResource(R.xml.preferences);
        ListPreference pref_locale = (ListPreference) findPreference(Settings.PREF_LOCALE);

        final CharSequence[] llist = pref_locale.getEntryValues();
        final CharSequence[] lnames = new CharSequence[llist.length];
        lnames[0] = getString(R.string.locale_default);
        for (int i = 1; i < llist.length; i++) {
            final Locale l = new Locale(llist[i].toString());
            final String name = l.getDisplayLanguage(l);
            lnames[i] = name.substring(0, 1).toUpperCase(l) + name.substring(1);
        }
        pref_locale.setEntries(lnames);

        for (String code : prefcodes) {
            final Preference pref = findPreference(code);
            pref.setOnPreferenceChangeListener(this);
            if (pref instanceof ListPreference)
                pref.setSummary(((ListPreference) pref).getEntry());
        }

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (pref.contains(Settings.PREF_LOCATION)) {
            Preference pref_location = findPreference(Settings.PREF_LOCATION);
            pref_location.setSummary(pref.getString(Settings.PREF_LOCATION, ""));
        }

        // FIXME: notification must be shown for application to work properly
        CheckBoxPreference notify = (CheckBoxPreference) findPreference(Settings.PREF_NOTIFY);
        notify.setEnabled(false);
        notify.setChecked(true);

        setHasOptionsMenu(true);
    }

    @Override public void onStart() {
        super.onStart();
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        }
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override public boolean onPreferenceTreeClick(PreferenceScreen screen,
                                                   Preference preference) {
        if (preference.getKey().equals(Settings.PREF_LOCATION)) {
            LocationFragment f = new LocationFragment();
            f.setChangeListener(this);
            getFragmentManager().beginTransaction()
                .replace(android.R.id.content, f)
                .addToBackStack("location")
                .commit();
            return true;
        }
        return super.onPreferenceTreeClick(screen, preference);
    }
}
