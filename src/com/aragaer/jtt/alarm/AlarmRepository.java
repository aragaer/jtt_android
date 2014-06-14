package com.aragaer.jtt.alarm;

import android.content.*;

public class AlarmRepository {
	private ContentResolver resolver;

	public AlarmRepository(Context context) {
		resolver = context.getContentResolver();
	}

	public void saveAlarm(Alarm alarm) {
		ContentValues values = new ContentValues();
		values.put(Alarm.JTT, alarm.jtt);
		resolver.insert(AlarmProvider.ALARM_URI, values);
	}
}
