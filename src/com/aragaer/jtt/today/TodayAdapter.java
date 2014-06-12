package com.aragaer.jtt.today;

import com.aragaer.jtt.R;
import com.aragaer.jtt.core.Hour;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.System;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/* Hour item in TodayList */
class HourItem extends TodayItem {
	public final int hnum;

	public HourItem(long t, int h) {
		super(t);
		hnum = h % 12;
	}

	static String[] extras;

	@Override
	public View toView(Context c, View v, int sel_p_diff) {
		if (v == null)
			v = View.inflate(c, R.layout.today_item, null);
		final StringResources sr = RuntimeResources.get(c).getInstance(
				StringResources.class);

		((TextView) v.findViewById(R.id.glyph)).setText(Hour.Glyphs[hnum]);
		((TextView) v.findViewById(R.id.name)).setText(sr.getHrOf(hnum));
		((TextView) v.findViewById(R.id.extra)).setText(extras[hnum]);
		((TextView) v.findViewById(R.id.curr)).setText(sel_p_diff == 0 ? "▶"
				: "");

		return v;
	}
}

/* Hour boundary item in TodayList */
class BoundaryItem extends TodayItem {
	public BoundaryItem(long t) {
		super(t);
	}

	enum NearSelected {
		DEFAULT, BEFORE, AFTER;
	}

	@Override
	public View toView(Context c, View v, int sel_p_diff) {
		if (v == null)
			v = View.inflate(c, R.layout.today_boundary_item, null);
		final StringResources sr = RuntimeResources.get(c).getInstance(
				StringResources.class);
		((TextView) v.findViewById(R.id.time)).setText(sr.format_time(time));
		((ImageView) v.findViewById(R.id.border))
				.setImageLevel(imageLevelFromDifference(sel_p_diff));
		return v;
	}

	private int imageLevelFromDifference(int difference) {
		NearSelected result;
		switch (difference) {
		case 1:
			result = NearSelected.AFTER;
			break;
		case -1:
			result = NearSelected.BEFORE;
			break;
		default:
			result = NearSelected.DEFAULT;
			break;
		}
		return result.ordinal();
	}
}

public class TodayAdapter extends ArrayAdapter<TodayItem> implements
		StringResources.StringResourceChangeListener {
	private static final int DAY_PART_COUNT = 3;
	private static final int TRANSITION_COUNT = DAY_PART_COUNT + 1;
	private static final int HOUR_COUNT = DAY_PART_COUNT * Hour.HOURS;
	private static final int ITEM_COUNT = HOUR_COUNT * 2 - 1;
	private final long transitions[] = new long[TRANSITION_COUNT];
	private int selected;

	public TodayAdapter(Context c, int layout_id) {
		super(c, layout_id);
		RuntimeResources
				.get(c)
				.getInstance(StringResources.class)
				.registerStringResourceChangeListener(
						this,
						StringResources.TYPE_HOUR_NAME
								| StringResources.TYPE_TIME_FORMAT);
		HourItem.extras = new String[] { c.getString(R.string.sunset), "", "",
				c.getString(R.string.midnight), "", "",
				c.getString(R.string.sunrise), "", "",
				c.getString(R.string.noon), "", "" };
		setNotifyOnChange(false);
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		return getItem(position).toView(parent.getContext(), v,
				selected - position);
	}

	/*
	 * takes a sublist of hours creates a list to display by adding day names
	 */
	private Collection<TodayItem> buildItems(boolean is_day) {
		List<TodayItem> result = new ArrayList<TodayItem>(ITEM_COUNT);
		int h_add = is_day ? 0 : Hour.HOURS;
		result.add(new HourItem(transitions[0], h_add));
		for (int i = 1; i < TRANSITION_COUNT; i++) {
			final long start = transitions[i - 1];
			final long diff = transitions[i] - start;
			for (int j = 1; j <= Hour.HOURS; j++) {
				result.add(new BoundaryItem(start + (j * 2 - 1) * diff
						/ Hour.HOURS / 2));
				result.add(new HourItem(start + j * diff / Hour.HOURS, h_add
						+ j));
			}
			h_add = Hour.HOURS - h_add;
		}
		return result;
	}

	public synchronized void setTimestamps(long tr[], boolean is_day) {
		long now = System.currentTimeMillis();

		if (now < tr[1] || now >= tr[2]) {
			clear();
			return;
		}

		if (now < transitions[1] || now >= transitions[2]
				|| !Arrays.equals(transitions, tr)) {
			System.arraycopy(tr, 0, transitions, 0, tr.length);
			clear();
			addAll(buildItems(is_day));
		}

		// odd items - boundaries
		selected = 0;
		while (getItem(selected + 1).time < now)
			selected += 2;

		notifyDataSetChanged();
	}

	@Override
	public boolean isEnabled(int pos) {
		return false;
	}

	public void onStringResourcesChanged(int changes) {
		notifyDataSetChanged();
	}

	@Override
	public int getViewTypeCount() {
		return 2; // hours and borders
	}

	@Override
	public int getItemViewType(int position) {
		return position % 2; // 0 for hours, 1 for borders
	}
}
