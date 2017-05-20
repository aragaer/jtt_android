package com.aragaer.jtt;

import com.aragaer.jtt.resources.StringResources;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;


public class JTTMainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
	setTheme(Settings.getAppTheme(this));
	super.onCreate(savedInstanceState);
	startService(new Intent(this, JttService.class));
	StringResources.setLocaleToContext(this);
	getFragmentManager().beginTransaction()
	    .replace(android.R.id.content, new MainFragment()).commit();
	final SharedPreferences pref = PreferenceManager
	    .getDefaultSharedPreferences(this);
	pref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
	StringResources.setLocaleToContext(this);
	super.onRestoreInstanceState(state);
    }

    @Override
    protected void onStart() {
	super.onStart();
	SharedPreferences pref = PreferenceManager
	    .getDefaultSharedPreferences(this);
	if (!pref.contains("jtt_loc")) // location is not set
	    openSettings();
    }

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
	if (key.equals(Settings.PREF_THEME) || key.equals(Settings.PREF_LOCALE)) {
	    final int flags = Intent.FLAG_ACTIVITY_NO_ANIMATION;
	    finish();
	    startActivity(getIntent().addFlags(flags));
	    // restart settings activity on top of this
	    startActivity(new Intent(this, Settings.class).addFlags(flags));
	}
    }

    private void openSettings() {
	startActivity(new Intent(this, Settings.class));
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
