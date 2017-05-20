package com.aragaer.jtt;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;


public class JTTMainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean reopenSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	setTheme(Settings.getAppTheme(this));
	super.onCreate(savedInstanceState);
	startService(new Intent(this, JttService.class));
	getFragmentManager().popBackStackImmediate("settings", FragmentManager.POP_BACK_STACK_INCLUSIVE);
	getFragmentManager().beginTransaction()
	    .replace(android.R.id.content, new MainFragment()).commit();
	final SharedPreferences pref = PreferenceManager .getDefaultSharedPreferences(this);
	pref.registerOnSharedPreferenceChangeListener(this);
	if (savedInstanceState != null && savedInstanceState.getInt("reopen settings", 0) == 1)
	    openSettings();
	else if (!pref.contains(Settings.PREF_LOCATION)) // location is not set
	    openSettings();
	reopenSettings = false;
    }

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
	if (key.equals(Settings.PREF_THEME) || key.equals(Settings.PREF_LOCALE)) {
	    reopenSettings = true;
	    recreate();
	}
    }

    @Override public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	if (reopenSettings)
	    outState.putInt("reopen settings", 1);
    }

    @Override public void onRestoreInstanceState(Bundle inState) {
	super.onRestoreInstanceState(inState);
	reopenSettings = inState.getInt("reopen settings", 0) == 1;
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
