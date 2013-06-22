package com.aragaer.jtt;

import java.util.TimeZone;

import com.aragaer.jtt.core.Calculator;
import com.aragaer.jtt.core.Hour;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/* A single item in TodayList */
abstract class TodayItem {
	public final long time;
	abstract public View toView(Context c);
	public TodayItem(long t) {
		time = t;
	}
}

/* Hour item in TodayList */
class HourItem extends TodayItem {
	public static int current;
	public static long prev_transition, next_transition;
	public final int hnum;

	public HourItem(long t, int h) {
		super(t);
		hnum = h % 12;
	}

	static String[] extras = null;

	public View toView(Context c) {
		if (extras == null) {
			extras = new String[] { c.getString(R.string.sunset), "", "",
					c.getString(R.string.midnight), "", "",
					c.getString(R.string.sunrise), "", "",
					c.getString(R.string.noon), "", "" };

		}
		View v = View.inflate(c, R.layout.today_item, null);

		boolean is_current = hnum == current
				&& time >= prev_transition
				&& time <= next_transition;

		final StringResources sr = RuntimeResources.get(c).getInstance(StringResources.class);

		((TextView) v.findViewById(R.id.time)).setText(sr.format_time(time));
		((TextView) v.findViewById(R.id.glyph)).setText(Hour.Glyphs[hnum]);
		((TextView) v.findViewById(R.id.name)).setText(sr.getHrOf(hnum));
		((TextView) v.findViewById(R.id.extra)).setText(extras[hnum]);
		((TextView) v.findViewById(R.id.curr)).setText(is_current ? "▶" : "");

		return v;
	}
}

/* "DayName" item in Today List */
class DayItem extends TodayItem {
	public DayItem(long t) {
		super(t);
	}

	public View toView(Context c) {
		View v = View.inflate(c, android.R.layout.preference_category, null);
		((TextView) v.findViewById(R.id.title)).setText(dateToString(time, c));

		return v;
	}

	/* returns strings like "today" or "2 days ago" etc */
	static String[] daynames = null;
	private String dateToString(long date, Context c) {
		if (daynames == null)
			daynames = new String[] { c.getString(R.string.day_next),
				c.getString(R.string.day_curr),
				c.getString(R.string.day_prev) };

		final long now = System.currentTimeMillis();
		final int ddiff = (int) (ms_to_day(now) - ms_to_day(date));
		if (ddiff < 2 && ddiff > -2)
			return daynames[ddiff+1];
		return c.getResources().getQuantityString(ddiff > 0
				? R.plurals.days_past
				: R.plurals.days_future, ddiff, ddiff);
	}

	/* helper function
	 * converts ms timestamp to day number
	 * not useful by itself but can be used to find difference between days
	 */
	private static final long ms_to_day(long t) {
		t += TodayAdapter.tz.getOffset(t);
		return t / Calculator.ms_per_day;
	}
}

public class TodayAdapter extends ArrayAdapter<TodayItem> implements
		StringResources.StringResourceChangeListener {
	private static final String TAG = "TODAY";
	private boolean is_day;
	private final long transitions[] = new long[4];
	static TimeZone tz = TimeZone.getDefault();

	public TodayAdapter(Context c, int layout_id) {
		super(c, layout_id);
		RuntimeResources.get(c).getInstance(StringResources.class)
				.registerStringResourceChangeListener(this,
						StringResources.TYPE_HOUR_NAME | StringResources.TYPE_TIME_FORMAT);
		HourItem.extras = DayItem.daynames = null;
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		return getItem(position).toView(parent.getContext());
	}

	/* helper function
	 * accepts a time stamp
	 * returns a time stamp for the same time but next day
	 *
	 * simply adding 24 hours does not always work
	 */
	private static final long add24h(long t) {
		t += tz.getOffset(t);
		t += Calculator.ms_per_day;
		return t - tz.getOffset(t);
	}

	/* takes a sublist of hours
	 * creates a list to display by adding day names
	 */
	void buildItems() {
		clear();

		/* time stamp for 00:00:00 */
		long start_of_day = transitions[0];

		/* "aligning" code */
		start_of_day += tz.getOffset(start_of_day);
		start_of_day -= start_of_day % Calculator.ms_per_day;
		start_of_day -= tz.getOffset(start_of_day);

		add(new DayItem(start_of_day)); // List should start with one
		start_of_day = add24h(start_of_day);

		int h_add = is_day ? 0 : 6;

		/* start with first transition */
		add(new HourItem(transitions[0], h_add));
		for (int i = 1; i < transitions.length; i++) {
			long start = transitions[i - 1];
			long diff = transitions[i] - start;
			for (int j = 1; j <= 6; j++) {
				long t = start + j * diff / 6;
				if (t >= start_of_day) {
					add(new DayItem(start_of_day));
					start_of_day = add24h(start_of_day);
				}
				add(new HourItem(t, h_add + j));
			}
			h_add = 6 - h_add;
		}
	}

	public void setCurrent(int cur) {
		long now = System.currentTimeMillis();
		HourItem.current = cur;

		/* Proper code to update timezone when it changed is quite complex
		 * The event itself is pretty rare
		 * Even if it breaks, restarting the app will fix that
		 */
		tz = TimeZone.getDefault();

		if (now >= transitions[1] && now < transitions[2]) {
			notifyDataSetChanged();
			return;
		}

		try {
			is_day = Calculator.getSurroundingTransitions(getContext(), now, transitions);
		} catch (IllegalStateException e) {
			Log.w(TAG, "Content provider has no location");
			// starting the JttService should solve this
			getContext().startService(new Intent(getContext(), JttService.class));
			return;
		}

		HourItem.prev_transition = transitions[1];
		HourItem.next_transition = transitions[2];

		buildItems();
	}


	@Override
	public boolean isEnabled(int pos) {
		return false;
	}

	public void onStringResourcesChanged(int changes) {
		notifyDataSetChanged();
	}
}
