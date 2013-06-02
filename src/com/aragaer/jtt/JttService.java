package com.aragaer.jtt;

import com.aragaer.jtt.core.Clockwork;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;
import com.aragaer.jtt.resources.StringResources.StringResourceChangeListener;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class JttService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String TAG = "JTT_SERVICE";
	private JttStatus status_notify;

	public class JttServiceBinder extends Binder {
		public JttService getService() {
			return JttService.this;
		}
	}
	private final JttServiceBinder binder = new JttServiceBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		Log.i(TAG, "Service starting");
		Clockwork.schedule(this);

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		pref.registerOnSharedPreferenceChangeListener(this);

		toggle_notify(pref.getBoolean("jtt_notify", true));

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service destroying");

		if (status_notify != null) {
			status_notify.release();
			status_notify = null;
		}
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
			Clockwork.schedule(this);
	}
}
