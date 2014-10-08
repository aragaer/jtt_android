package com.aragaer.jtt;

import com.aragaer.jtt.core.TransitionProvider;
import com.aragaer.jtt.clockwork.AndroidClock;
import com.aragaer.jtt.clockwork.Clock;

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
	private final Clock clock;

	public JttService() {
		clock = new AndroidClock(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service starting");
		move();

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		pref.registerOnSharedPreferenceChangeListener(this);

		toggle_notify(pref.getBoolean("jtt_notify", true));

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
		else if (key.equals(Settings.PREF_WIDGET)
				|| key.equals(Settings.PREF_LOCALE)
				|| key.equals(Settings.PREF_HNAME))
			WidgetProvider.draw_all(this);
	}

	private void move() {
		final float l[] = Settings.getLocation(this);
		final ContentValues location = new ContentValues(2);
		location.put("lat", l[0]);
		location.put("lon", l[1]);
		getContentResolver().update(TransitionProvider.LOCATION, location, null, null);
		clock.adjust();
	}
}
