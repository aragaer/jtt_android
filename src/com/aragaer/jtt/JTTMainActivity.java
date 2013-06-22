package com.aragaer.jtt;

import com.aragaer.jtt.core.Clockwork;

import android.app.ActivityGroup;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.widget.ListView;

public class JTTMainActivity extends ActivityGroup {
    private ClockView clock;
    private JTTPager pager;
    private TodayAdapter today;

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(Clockwork.ACTION_JTT_TICK))
				return;
			final int n = intent.getIntExtra("hour", 0);
			final int wrapped = intent.getIntExtra("jtt", 0);

			clock.setHour(wrapped);
			today.setCurrent(n);
		}
	};

    private int settings_tab = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Settings.getTheme(this));
        super.onCreate(savedInstanceState);
        startService(new Intent(this, JttService.class));

        pager = new JTTPager(this);

        clock = new ClockView(this);
        pager.addTab(clock, R.string.clock);

        final ListView today_list = new ListView(this);
        today = new TodayAdapter(this, 0);
        today_list.setAdapter(today);
        pager.addTab(today_list, R.string.today);

        final Window sw = getLocalActivityManager().startActivity("settings",
                new Intent(this, Settings.class));
        settings_tab = pager.addTab(sw.getDecorView(), R.string.settings);

        setContentView(pager);

		registerReceiver(receiver, new IntentFilter(Clockwork.ACTION_JTT_TICK));
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        if (settings.getBoolean("jtt_first", true)) {
            settings.edit().putBoolean("jtt_first", false).commit();
            if (settings.contains("jtt_loc")) // it's already configured
                return;
            pager.selectScreen(settings_tab);
            PreferenceActivity pa = (PreferenceActivity) getLocalActivityManager()
                    .getActivity("settings");
            ((LocationPreference) pa.findPreference("jtt_loc")).showMe();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
		unregisterReceiver(receiver);
    }
}
