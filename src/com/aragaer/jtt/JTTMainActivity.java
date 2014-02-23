package com.aragaer.jtt;

import com.aragaer.jtt.core.Clockwork;
import com.aragaer.jtt.resources.StringResources;
import com.aragaer.jtt.today.TodayAdapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class JTTMainActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private ClockView clock;
	private TodayAdapter today;

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(Clockwork.ACTION_JTT_TICK))
				return;
			final int wrapped = intent.getIntExtra("jtt", 0);

			clock.setHour(wrapped);
			today.tick(intent.getLongArrayExtra("tr"), intent.getBooleanExtra("day", false));
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(Settings.getAppTheme(this));
		super.onCreate(savedInstanceState);
		startService(new Intent(this, JttService.class));
		StringResources.setLocaleToContext(this);
		final ViewPager pager = new ViewPager(this);
		final ViewPagerAdapter pager_adapter = new ViewPagerAdapter(this, pager);

		clock = new ClockView(this);

		final ListView today_list = new ListView(this);
		today = new TodayAdapter(this, 0);
		today_list.setAdapter(today);
		today_list.setDividerHeight(-getResources().getDimensionPixelSize(R.dimen.today_divider_neg));

		pager_adapter.addView(clock, R.string.clock);
		pager_adapter.addView(today_list, R.string.today);

		pager.setAdapter(pager_adapter);
		setContentView(pager);

		registerReceiver(receiver, new IntentFilter(Clockwork.ACTION_JTT_TICK));
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
			startActivity(new Intent(this, Settings.class));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem settings = menu.add(R.string.settings);
		settings.setIntent(new Intent(this, Settings.class));
		return super.onCreateOptionsMenu(menu);
	}
}
