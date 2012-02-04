package com.aragaer.jtt;


import android.app.Activity;
import android.app.ActivityGroup;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class JTTMainActivity extends ActivityGroup {
    private static final int btn_ids[] = { R.id.clockbtn, R.id.alarmbtn,
            R.id.settingsbtn };

    private IJTTService api;
    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("client", "Service connection established");
            api = IJTTService.Stub.asInterface(service);
            mHandler.postDelayed(mUpdateUITimerTask, 1000);
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.i("client", "Service connection closed");
        }
    };

    private final Runnable mUpdateUITimerTask = new Runnable() {
        public void run() {
            try {
                clock.setJTTHour(api.getHour());
            } catch (RemoteException e) {
                Log.d("jtt client", "service killed");
            }
            mHandler.postDelayed(mUpdateUITimerTask, 60 * 1000L);
        }
    };
    private final Handler mHandler = new Handler();

    private JTTClockView clock;
    private JTTPager pager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent service = new Intent(JTTService.class.getName());
        startService(service);

        setContentView(R.layout.main);

        Button tabs[] = new Button[btn_ids.length];
        for (int i = 0; i < btn_ids.length; i++)
            tabs[i] = (Button) findViewById(btn_ids[i]);

        clock = (JTTClockView) findViewById(R.id.hour);
        pager = (JTTPager) findViewById(R.id.tabcontent);
        final Window sw = getLocalActivityManager().startActivity("settings",
                             new Intent(this, JTTSettingsActivity.class));
                        pager.addView(sw.getDecorView());
        if (savedInstanceState != null)
            pager.mCurrentScreen = savedInstanceState.getInt("Screen");
        pager.setTabs(tabs);
        
        bindService(service, conn, 0);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("Screen", pager.mCurrentScreen);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unbindService(conn);
        } catch (Throwable t) {
            Log.w("jtt client", "Failed to unbind from the service", t);
        }

        mHandler.removeCallbacks(mUpdateUITimerTask);
        Log.i("jtt client", "Activity destroyed");
    }

    public void onToggle(View view) {
        pager.btnToggle((Button) view);
    }
}
