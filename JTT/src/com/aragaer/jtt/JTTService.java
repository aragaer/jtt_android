package com.aragaer.jtt;

import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class JTTService extends Service {
    private JTT calculator;
    private JTTHour hour = new JTTHour(0);

    private static final String TAG = JTTService.class.getSimpleName();

    private IJTTService.Stub apiEndpoint = new IJTTService.Stub() {
        public JTTHour getHour() {
            synchronized (hour) {
                return hour;
            }
        }
    };

    private Timer timer;
    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            hour = calculator.time_to_jtt(new Date());
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

    @Override
    public void onStart(Intent intent, int startid) {
        float latitude, longitude;
        Log.i(TAG, "Service starting");
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        latitude = Float.parseFloat(settings.getString("posLat", "0.0"));
        longitude = Float.parseFloat(settings.getString("posLong", "0.0"));

        calculator = new JTT(latitude, longitude, TimeZone.getDefault());
        hour = calculator.time_to_jtt(new Date());
        Log.d(TAG, "rate = "+calculator.rate);
        Log.d(TAG, "Next hour at "+calculator.nextHour.toLocaleString());

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
    }
}
