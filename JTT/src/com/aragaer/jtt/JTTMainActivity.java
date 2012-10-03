package com.aragaer.jtt;

import java.lang.ref.WeakReference;

import android.app.ActivityGroup;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.LinearLayout;

public class JTTMainActivity extends ActivityGroup {
    private final static String TAG = "jtt main";

    private JTTClockView clock;
    private JTTPager pager;
    private JTTTodayList today;

    private Messenger mService = null;
    final Messenger mMessenger = new Messenger(new IncomingHandler(this));

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
                main.get().clock.setHour(msg.arg1, msg.arg2);
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

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null,
                        JTTService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
                Log.i(TAG, "Service connection established");
                today.onServiceConnect();
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do
                // anything with it
                Log.i(TAG, "Service connection can't be established");
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            Log.i(TAG, "Service connection closed");
        }
    };

    protected void send_msg_to_service(int what, Bundle b) {
        Message msg = Message.obtain(null, what);
        if (b != null)
            msg.setData(b);

        msg.replyTo = mMessenger;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            Log.i(TAG, "Service connection broken");
        } catch (NullPointerException e) {
            Log.i(TAG, "Service not connected");
        }
    }

    int settings_tab = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent service = new Intent(JTTService.class.getName());
        startService(service);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);

        pager = new JTTPager(this, null);
        pager.setLayoutParams(lp);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            pager.setOrientation(LinearLayout.VERTICAL);
        pager.setPadding(5, 5, 5, 5);

        clock = new JTTClockView(this);
        clock.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        clock.setLayoutParams(lp);
        pager.addTab(this, clock, getString(R.string.clock));

        today = new JTTTodayList(this);
        today.setLayoutParams(lp);
        pager.addTab(this, today, getString(R.string.today));

        final Window sw = getLocalActivityManager().startActivity("settings",
                new Intent(this, JTTSettingsActivity.class));
        settings_tab = pager.addTab(this, sw.getDecorView(), getString(R.string.settings));

        setContentView(pager);

        if (savedInstanceState != null)
            pager.scrollToScreen(savedInstanceState.getInt("Screen"));

        bindService(service, conn, 0);
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
            pager.scrollToScreen(settings_tab);
            PreferenceActivity pa = (PreferenceActivity) getLocalActivityManager()
                    .getActivity("settings");
            ((LocationPreference) pa.findPreference("jtt_loc")).showMe();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("Screen", pager.getScreen());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        send_msg_to_service(JTTService.MSG_UNREGISTER_CLIENT, null);

        try {
            unbindService(conn);
        } catch (Throwable t) {
            Log.w(TAG, "Failed to unbind from the service", t);
        }

        Log.i(TAG, "Activity destroyed");
    }
}
