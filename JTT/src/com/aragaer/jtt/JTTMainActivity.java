package com.aragaer.jtt;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class JTTMainActivity extends TabActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Reusable TabSpec for each tab
		Intent intent; // Reusable intent for each tab
		
		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, JTTActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("Time")
				.setIndicator("Time", res.getDrawable(R.drawable.ic_tab_time))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, JTTAlarmActivity.class);

		spec = tabHost.newTabSpec("Alarm")
				.setIndicator("Alarm", res.getDrawable(R.drawable.ic_tab_time))
				.setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, JTTSettingsActivity.class);

		spec = tabHost.newTabSpec("Settings")
				.setIndicator("Settings", res.getDrawable(R.drawable.ic_tab_time))
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}
}
