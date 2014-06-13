package com.aragaer.jtt.alarm;

import android.database.Cursor;

import com.aragaer.jtt.core.Hour;

public class Alarm {
	public static final String ID_FIELD = "_id";
	public static final String JTT = "jtt";
	public int jtt;

	public Alarm(Hour hour) {
		jtt = hour.wrapped;
	}

	private Alarm(int wrapped) {
		jtt = wrapped;
	}

	public static Alarm fromCursor(Cursor data) {
		int jttFieldIndex = data.getColumnIndex(JTT);
		return new Alarm(data.getInt(jttFieldIndex));
	}
}
