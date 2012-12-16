package com.aragaer.jtt;

import java.lang.ref.WeakReference;

import android.app.ActivityGroup;
import android.content.Intent;
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
    private final static String TAG = "jtt main";

    private JTTClockView clock;
    private JTTPager pager;
    private TodayAdapter today;

    protected JTTUtil.ConnHelper conn = new JTTUtil.ConnHelper(this, new IncomingHandler(this));

    static class IncomingHandler extends Handler {
        private final WeakReference<JTTMainActivity> main;

        public IncomingHandler(JTTMainActivity m) {
            main = new WeakReference<JTTMainActivity>(m);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case JTTService.MSG_HOUR:
                main.get().today.setCurrent(msg.arg1);
                /* fall-through! */
            case JTTService.MSG_SUBTICK:
//                main.get().clock.setHour(msg.arg1, msg.arg2);
                break;
            case JTTService.MSG_TRANSITIONS:
                main.get().today.addTransitions(msg.getData());
                break;
            case JTTService.MSG_INVALIDATE:
                Log.d(TAG, "Invalidate all");
                main.get().today.reset();
                break;
            default:
                super.handleMessage(msg);
                break;
            }
        }
    }

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
        conn.bind(service, 0);
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
        conn.release();

        Log.i(TAG, "Activity destroyed");
    }
}
