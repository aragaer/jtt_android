// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
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
