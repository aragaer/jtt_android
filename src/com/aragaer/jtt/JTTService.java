package com.aragaer.jtt;

import com.aragaer.jtt.core.Clockwork;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;
import com.aragaer.jtt.resources.StringResources.StringResourceChangeListener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class JTTService extends Service implements StringResourceChangeListener {
    private static final String TAG = JTTService.class.getSimpleName();
    private NotificationManager nm;
    private static final int flags_ongoing = Notification.FLAG_ONGOING_EVENT
            | Notification.FLAG_NO_CLEAR;
    private static final int APP_ID = 0;

    private PendingIntent pending_main;
    private StringResources sr = null;

    private boolean notify, force_stop = false;
    private static Notification notification;

    private long sync = 0;
    private long start_day, end_day;
    private String t_start, t_end;

    private int hour, sub;

    private String app_name;
    private void notify_helper(int hn, int hf) {
        notification = new Notification(R.drawable.notification_icon,
                app_name, System.currentTimeMillis());
        RemoteViews rv = new RemoteViews(getPackageName(),
                R.layout.notification);

        notification.flags = flags_ongoing;
        notification.iconLevel = hn;
        rv.setTextViewText(R.id.image, JTTHour.Glyphs[hn]);
        rv.setTextViewText(R.id.title, sr.getHrOf(hn));
        rv.setTextViewText(R.id.percent, String.format("%d%%", hf));
        rv.setProgressBar(R.id.fraction, 100, hf, false);
        rv.setTextViewText(R.id.start, t_start);
        rv.setTextViewText(R.id.end, t_end);

        notification.contentIntent = pending_main;
        notification.contentView = rv;
        nm.notify(APP_ID, notification);
    }
    
    private void doNotify(int n, int f, int event) {
        if (notify)
            notify_helper(n, f);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Log.d(TAG, "Service starting");
        if (sr == null) // first run
            init();
    }

    private void init() {
        Log.d(TAG, "Service initializing");
        sr = RuntimeResources.get(this).getInstance(StringResources.class);
		sr.registerStringResourceChangeListener(this,
				StringResources.TYPE_HOUR_NAME
						| StringResources.TYPE_TIME_FORMAT);
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        final float[] l = Settings.getLocation(this);

        Intent JTTMain = new Intent(getBaseContext(), JTTMainActivity.class);
        pending_main = PendingIntent.getActivity(this, 0, JTTMain, 0);
        notify = settings.getBoolean("jtt_notify", true);
        app_name = getString(R.string.app_name);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (!notify)
            nm.cancel(APP_ID);

		startService(new Intent(Clockwork.ACTION_ENABLE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroying");

        if (force_stop)
            nm.cancel(APP_ID);
        else {
            SharedPreferences settings = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());
            final boolean boot = settings.getBoolean("jtt_bootup", true);
            if (notify || boot) {
                notification = new Notification(R.drawable.notification_icon,
                        app_name, System.currentTimeMillis());

                notification.setLatestEventInfo(JTTService.this, getString(R.string.srv_fail),
                        getString(R.string.srv_fail_ex), pending_main);
                notification.flags = boot ? flags_ongoing : 0;
                nm.notify(APP_ID, notification);
            }
        }
    }

	public void onStringResourcesChanged(final int changes) {
        if (notify)
            notify_helper(hour, sub);
	}
}
