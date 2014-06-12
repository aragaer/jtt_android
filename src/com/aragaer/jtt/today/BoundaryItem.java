package com.aragaer.jtt.today;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aragaer.jtt.R;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;

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
			result = NearSelected.BEFORE;
			break;
		case -1:
			result = NearSelected.AFTER;
			break;
		default:
			result = NearSelected.DEFAULT;
			break;
		}
		return result.ordinal();
	}
}