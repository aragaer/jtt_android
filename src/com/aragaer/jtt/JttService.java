package com.aragaer.jtt;

import com.aragaer.jtt.core.Calculator;
import com.aragaer.jtt.core.Clockwork;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class JttService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String TAG = "JTT_SERVICE";
	private JttStatus status_notify;

	static final String STOP_ACTION = "com.aragaer.jtt.action.SERVICE_STOP";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		Log.i(TAG, "Service starting");
		if (intent != null && STOP_ACTION.equals(intent.getAction())) {
			if (status_notify != null) {
				status_notify.release();
				status_notify = null;
			}
			return START_NOT_STICKY;
		}
		move();

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		pref.registerOnSharedPreferenceChangeListener(this);

		toggle_notify(pref.getBoolean("jtt_notify", true));

		return START_STICKY;
	}

	private void toggle_notify(final boolean notify) {
		if (status_notify == null) {
			if (notify)
				status_notify = new JttStatus(this);
		} else {
			if (!notify) {
				status_notify.release();
				status_notify = null;
			}
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		if (key.equals(Settings.PREF_NOTIFY))
			toggle_notify(pref.getBoolean("jtt_notify", true));
		else if (key.equals(Settings.PREF_LOCATION))
			move();
		else if (key.equals(Settings.PREF_WIDGET_INVERSE)
				|| key.equals(Settings.PREF_LOCALE)
				|| key.equals(Settings.PREF_HNAME))
			JTTWidgetProvider.draw_all(this);
	}

	private void move() {
		final float l[] = Settings.getLocation(this);
		final ContentValues location = new ContentValues(2);
		location.put("lat", l[0]);
		location.put("lon", l[1]);
		getContentResolver().update(Calculator.LOCATION, location, null, null);
		Clockwork.schedule(this);
	}
}
