package com.aragaer.jtt;

import java.util.Date;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public final class JTTAlarm {
	public long id = -1;
	public JTTHour jtt_time;
	public Date time;
	public String name;
	public String repeat;
	public String tone;
	public Boolean disabled = true;

	public JTTAlarm(JTTHour hour, String n) {
		jtt_time = hour;
		name = n;
	}

	public JTTAlarm(Date t, String n) {
		time = t;
		name = n;
	}

	public static JTTAlarm load(Context ctx, long id) {
		Uri uri = ContentUris.withAppendedId(AlarmProvider.CONTENT_URI, id);
		Cursor c = ctx.getContentResolver().query(uri,
				AlarmProvider.Alarms.PROJECTION_ALL, null, null, null);
		if (c == null)
			return null;
		if (!c.moveToFirst()) {
			c.close();
			return null;
		}

		JTTAlarm res;
		// { _ID, NAME, JTT, TIME, REPEAT, TONE, ENABLED }
		String name = c.getString(1);
		if (c.isNull(2))
			res = new JTTAlarm(new Date(c.getLong(3)), name);
		else {
			int n = c.getInt(2);
			res = new JTTAlarm(new JTTHour(n / 100, n % 100), name);
		}

		res.id = id;
		res.repeat = c.getString(4);
		res.tone = c.getString(5);
		res.disabled = c.getInt(6) == 0;

		return res;
	}

	public static void save(Context ctx, JTTAlarm alarm) {
		ContentValues row = new ContentValues();
		if (alarm.jtt_time == null)
			row.put(AlarmProvider.Alarms.TIME, alarm.time.getTime());
		else
			row.put(AlarmProvider.Alarms.JTT, alarm.jtt_time.num * 100
					+ alarm.jtt_time.fraction);

		row.put(AlarmProvider.Alarms.NAME, alarm.name);
		row.put(AlarmProvider.Alarms.REPEAT, alarm.repeat);
		row.put(AlarmProvider.Alarms.TONE, alarm.tone);
		row.put(AlarmProvider.Alarms.ENABLED, alarm.disabled ? 0 : 1);

		if (alarm.id == -1)
			ctx.getContentResolver().insert(AlarmProvider.CONTENT_URI, row);
		else
			ctx.getContentResolver().update(
					ContentUris.withAppendedId(AlarmProvider.CONTENT_URI,
							alarm.id), row, null, null);
	}
}