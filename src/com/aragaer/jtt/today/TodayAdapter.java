package com.aragaer.jtt.today;

import com.aragaer.jtt.R;
import com.aragaer.jtt.core.FourTransitions;
import com.aragaer.jtt.core.Hour;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.lang.System;
import java.util.*;

public class TodayAdapter extends ArrayAdapter<TodayItem> implements
		StringResources.StringResourceChangeListener {
	private static final int DAY_PART_COUNT = 3;
	private static final int HOUR_COUNT = DAY_PART_COUNT * Hour.HOURS;
	private static final int ITEM_TYPE_COUNT = 2;
	private static final int ITEM_COUNT = HOUR_COUNT * ITEM_TYPE_COUNT - 1;
	private FourTransitions transitions;
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
	private Collection<TodayItem> buildItems() {
		List<TodayItem> result = new ArrayList<TodayItem>(ITEM_COUNT);
		result.add(new HourItem(transitions.previousStart, transitions.isDayCurrently ? 0
				: Hour.HOURS));
		result.addAll(addInterval(transitions.previousStart,
				transitions.currentStart, transitions.isDayCurrently));
		result.addAll(addInterval(transitions.currentStart,
				transitions.currentEnd, !transitions.isDayCurrently));
		result.addAll(addInterval(transitions.currentEnd,
				transitions.nextEnd, transitions.isDayCurrently));
		return result;
	}

	private Collection<TodayItem> addInterval(long start, long end,
			boolean isDay) {
		List<TodayItem> result = new ArrayList<TodayItem>(Hour.HOURS
				* ITEM_TYPE_COUNT);
		int h_add = isDay ? 0 : Hour.HOURS;
		long diff = end - start;
		for (int j = 1; j <= Hour.HOURS; j++) {
			result.add(new BoundaryItem(start + (j * 2 - 1) * diff / Hour.HOURS
					/ 2));
			result.add(new HourItem(start + j * diff / Hour.HOURS, h_add + j));
		}
		return result;
	}

	public synchronized void setTransitions(FourTransitions newTransitions) {
		long now = System.currentTimeMillis();

		if (newTransitions.notInCurrentInterval(now)) {
			clear();
			return;
		}

		if (!newTransitions.equals(transitions)) {
			transitions = newTransitions;
			clear();
			addAll(buildItems());
		}

		selected = 0;
		while (getItem(selected + 1).time < now)
			selected += ITEM_TYPE_COUNT;

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
		return ITEM_TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		return position % ITEM_TYPE_COUNT;
	}
}
