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
	private JTTHour hour;

	private static final String TAG = JTTService.class.getSimpleName();

	private Timer timer;
	private TimerTask updateTask = new TimerTask() {
		@Override
		public void run() {
			Log.i(TAG, "Timer task doing work");
			hour = calculator.time_to_jtt(new Date());
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
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

		timer = new Timer("JTTServiceTimer");
		timer.schedule(updateTask, 1000L, 60 * 1000L);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service destroying");

		timer.cancel();
		timer = null;
	}
}
