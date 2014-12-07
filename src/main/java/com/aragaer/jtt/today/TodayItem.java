package com.aragaer.jtt.today;

import android.content.Context;
import android.view.View;

/* A single item in TodayList */
public abstract class TodayItem {
	public final long time;
	abstract public View toView(Context c, View convert,
		int sel_p_diff /* difference from selected position */);

	public TodayItem(long t) {
		time = t;
	}
}