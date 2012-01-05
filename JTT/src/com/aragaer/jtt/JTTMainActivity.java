package com.aragaer.jtt;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

public class JTTMainActivity extends TabActivity {
	TabHost mTabHost;

	private void setupTab(final View view, final String tag, final Intent intent) {
		View tabview = createTabView(mTabHost.getContext(), tag);
		TabHost.TabSpec setContent = mTabHost.newTabSpec(tag)
				.setIndicator(tabview).setContent(intent);
		mTabHost.addTab(setContent);
	}

	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context)
				.inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mTabHost = getTabHost(); // The activity TabHost

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
	        getTabWidget().setOrientation(LinearLayout.VERTICAL);

		Intent intent;

		intent = new Intent().setClass(this, JTTActivity.class);
		setupTab(new TextView(this), "Clock", intent);
		intent = new Intent().setClass(this, JTTAlarmActivity.class);
		setupTab(new TextView(this), "Alarm", intent);
		intent = new Intent().setClass(this, JTTSettingsActivity.class);
		setupTab(new TextView(this), "Settings", intent);
		mTabHost.setCurrentTab(0);
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}
}
