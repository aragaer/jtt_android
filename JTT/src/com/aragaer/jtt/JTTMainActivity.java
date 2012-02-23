package com.aragaer.jtt;

import android.app.ActivityGroup;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class JTTMainActivity extends ActivityGroup {
    private final static String TAG = "jtt main";

    private JTTClockView clock;
    private JTTPager pager;
    private JTTHelp help;

    private Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null,
                        JTTService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
                Log.i(TAG, "Service connection established");
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
        msg.replyTo = mMessenger;
        if (b != null)
            msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            Log.i(TAG, "Service connection broken");
        } catch (NullPointerException e) {
            Log.i(TAG, "Service not connected");
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case JTTService.MSG_HOUR:
                JTTHour hour = new JTTHour(msg.arg1);
                hour.fraction = msg.arg2 / 100.0f;
                clock.setJTTHour(hour);
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent service = new Intent(JTTService.class.getName());
        startService(service);
        final LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);

        pager = (JTTPager) new JTTPager(this, null);
        pager.setLayoutParams(lp);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            pager.setOrientation(LinearLayout.VERTICAL);

        clock = new JTTClockView(this);
        clock.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        clock.setLayoutParams(lp);
        pager.addTab(clock, getString(R.string.clock));

        final Window sw = getLocalActivityManager().startActivity("settings",
                new Intent(this, JTTSettingsActivity.class));
        pager.addTab(sw.getDecorView(), getString(R.string.settings));

        help = new JTTHelp(this);
        help.setLayoutParams(lp);
        pager.addTab(help, getString(R.string.help));

        setContentView(pager);

        if (savedInstanceState != null)
            pager.scrollToScreen(savedInstanceState.getInt("Screen"));

        bindService(service, conn, 0);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("Screen", pager.getScreen());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unbindService(conn);
        } catch (Throwable t) {
            Log.w(TAG, "Failed to unbind from the service", t);
        }

        Log.i(TAG, "Activity destroyed");
    }
}
