package com.aragaer.jtt;

import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class JTTService extends Service {
    private JTT calculator;
    private JTTHour hour = new JTTHour(0);
    private Notification notification;
    private NotificationManager nm;
    private static final int APP_ID = 0;
    private Intent JTTMain;
    private PendingIntent pending_main;
    private SharedPreferences settings;
    private Boolean do_notify;

    static final int MSG_TOGGLE_NOTIFY = 0;
    private static final String TAG = JTTService.class.getSimpleName();

    private IJTTService.Stub apiEndpoint = new IJTTService.Stub() {
        public JTTHour getHour() {
            synchronized (hour) {
                return hour;
            }
        }
        public void startNotifying() {
            do_notify = true;
            notify_helper();
        }
        public void stopNotifying() {
            do_notify = false;
            notify_helper();
        }
        public void stopService() {
            synchronized (timer) {
                stopSelf();
            }
        }
    };

    private synchronized void notify_helper() {
        final Context ctx = getBaseContext();
        Log.d("service", "notify helper!");
        if (do_notify) {
            notification.setLatestEventInfo(JTTService.this,
                    ctx.getString(R.string.hr_of)+" "+hour.hour,
                    Math.round(hour.fraction * 100)+"%",
                    pending_main);
            notification.when = System.currentTimeMillis();
            notification.iconLevel = hour.num;
            nm.notify(APP_ID, notification);
        } else {
            nm.cancel(APP_ID);
        }
    }

    private Timer timer;
    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            hour = calculator.time_to_jtt(new Date());
            if (do_notify)
                notify_helper();
        }
    };

    public JTTHour getHour() {
        return hour;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (JTTService.class.getName().equals(intent.getAction())) {
            Log.d(TAG, "Bound by intent " + intent);
            return apiEndpoint;
        } else {
            return null;
        }
    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            Log.d("service", "message "+msg.what);
            switch (msg.what) {
            case MSG_TOGGLE_NOTIFY:
                do_notify = settings.getBoolean("jtt_notify", true);
                notify_helper();
            default:
                super.handleMessage(msg);
            }
        }
    }

    private void set_lat_lon() {
        float latitude, longitude;
        latitude = Float.parseFloat(settings.getString("jtt_lat", "0.0"));
        longitude = Float.parseFloat(settings.getString("jtt_lon", "0.0"));

        calculator = new JTT(latitude, longitude, TimeZone.getDefault());
        hour = calculator.time_to_jtt(new Date());
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Log.i(TAG, "Service starting");
        settings = this.getSharedPreferences("JTT", Context.MODE_WORLD_READABLE);
        set_lat_lon();
        Log.d(TAG, "rate = "+calculator.rate);
        Log.d(TAG, "Next hour at "+calculator.nextHour.toLocaleString());

        JTTMain = new Intent(getBaseContext(), JTTMainActivity.class);
        pending_main = PendingIntent.getActivity(getBaseContext(), 0, JTTMain, 0);

        do_notify = settings.getBoolean("jtt_notify", true);

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new Notification(R.drawable.notification_icon,
                getBaseContext().getString(R.string.app_name), System.currentTimeMillis());
        notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

        timer = new Timer("JTTServiceTimer");
        try {
            timer.scheduleAtFixedRate(updateTask, 0, 60 * 1000L);
        } catch (IllegalStateException e) {
            Log.i(TAG, "Timer is already running");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroying");

        timer.cancel();
        timer = null;

        notification.setLatestEventInfo(JTTService.this,
                getBaseContext().getString(R.string.srv_fail),
                getBaseContext().getString(R.string.srv_fail_ex),
                pending_main);
        notification.when = System.currentTimeMillis();
        notification.iconLevel = hour.num;
        if (do_notify)
            notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        else
            notification.flags = 0;
        nm.notify(APP_ID, notification);
    }
}
