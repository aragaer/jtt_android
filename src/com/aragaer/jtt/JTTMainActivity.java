package com.aragaer.jtt;


import android.app.ActivityGroup;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.widget.ListView;

public class JTTMainActivity extends ActivityGroup {
    private final static String TAG = "jtt main";

    private JTTClockView clock;
    private JTTPager pager;
    private TodayAdapter today;
    private JttReceiver receiver = new JttReceiver() {
		@Override
		void handle_tick(int n, int q, int f) {
			today.setCurrent(n);
			clock.setHour(n, q, f);
		}
	};

    int settings_tab = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        JTTUtil.setTheme(this);
        super.onCreate(savedInstanceState);
        final Intent service = new Intent(this, JTTService.class);
        startService(service);

        JTTUtil.initLocale(this);

        pager = new JTTPager(this);

        clock = new JTTClockView(this);
        pager.addTab(clock, R.string.clock);

        ListView today_list = new ListView(this);
        today = new TodayAdapter(this, 0);
        today_list.setAdapter(today);
        pager.addTab(today_list, R.string.today);

        final Window sw = getLocalActivityManager().startActivity("settings",
                new Intent(this, JTTSettingsActivity.class));
        settings_tab = pager.addTab(sw.getDecorView(), R.string.settings);

        setContentView(pager);
        receiver.register(this);
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
        receiver.unregister();
        today.unbind();

        Log.i(TAG, "Activity destroyed");
    }
}
