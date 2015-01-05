package com.aragaer.jtt.clockwork.android;
// vim: et ts=4 sts=4 sw=4

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


public class TickService extends IntentService {

	public TickService() {
		super("TICK");
	}

    private static ClockTickCallback callback;

    public static void setCallback(ClockTickCallback callback) {
        TickService.callback = callback;
    }

    protected void onHandleIntent(Intent intent) {
        callback.onTick();
        stopSelf();
    }

	private static final int INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT;

    public static void start(Context context, long start, long tickLength) {
        if (TickService.callback == null)
            throw new IllegalStateException("Must set callback first");
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent TickActionInternal = new Intent(context, TickService.class);
		am.setRepeating(AlarmManager.RTC, start, tickLength,
                        PendingIntent.getService(context, 0, TickActionInternal, INTENT_FLAGS));
    }

    public static void stop(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent TickActionInternal = new Intent(context, TickService.class);
		am.cancel(PendingIntent.getService(context, 0, TickActionInternal, 0));
    }
}
