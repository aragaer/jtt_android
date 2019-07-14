package com.aragaer.jtt;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;


public class JTTMainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override public void onCreate(Bundle savedInstanceState) {
        setTheme(Settings.getAppTheme(this));
        super.onCreate(savedInstanceState);
        startService(new Intent(this, JttService.class));
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);
        if (savedInstanceState == null) { // Otherwise we think that fragments are saved/restored
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new MainFragment()).commit();
            if (!pref.contains(Settings.PREF_LOCATION)) // location is not set
                openSettings();
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if (key.equals(Settings.PREF_THEME) || key.equals(Settings.PREF_LOCALE))
            recreate();
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void openSettings() {
        getFragmentManager().popBackStackImmediate("settings", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .addToBackStack("settings")
                .commit();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.settings);
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        openSettings();
        return true;
    }
}
