package com.aragaer.jtt;

import com.aragaer.jtt.core.Hour;
import com.aragaer.jtt.graphics.ArrowView;
import com.aragaer.jtt.graphics.Paints;
import com.aragaer.jtt.graphics.WadokeiView;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.TypedValue;

public class ClockView extends ViewGroup implements StringResources.StringResourceChangeListener {
	private final StringResources sr;
	private final TextView text;
	private final WadokeiView wadokei;
	private final ArrowView arrow;
	private final Hour hour = new Hour(0);
	private boolean vertical;

	public ClockView(Context context) {
		super(context);
		sr = RuntimeResources.get(context).getInstance(StringResources.class);
		sr.registerStringResourceChangeListener(this, StringResources.TYPE_HOUR_NAME);
		final Paints paints = Paints.forApplication(context);
		wadokei = new WadokeiView(context, paints);
		arrow = new ArrowView(context, paints);
		text = new TextView(context);

		text.setTextColor(Color.WHITE);
		text.setGravity(Gravity.CENTER);

		addView(wadokei);
		addView(arrow);
		addView(text);
	}

	private static final int granularity = 4;
	public void setHour(final int wrapped) {
		if (!hour.compareAndUpdate(wrapped, granularity))
			return; // do nothing
		wadokei.set_hour(hour);
		text.setText(vertical ? sr.getHrOf(hour.num) : sr.getHour(hour.num));
	}

	protected void onMeasure(int wms, int hms) {
		final int w = MeasureSpec.getSize(wms);
		final int h = MeasureSpec.getSize(hms);
		vertical = h > w;
		text.setText(vertical ? sr.getHrOf(hour.num) : sr.getHour(hour.num));
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, vertical ? w / 20 : w / 15);
		text.measure(0, 0);
		setMeasuredDimension(w, h);
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int w = r - l;
		final int h = b - t;
		if (vertical) {
			if (changed) {
				wadokei.layout(0, h - w, w, h - w / 20);
				arrow.layout(w * 19 / 40, h - w - w/20, w * 21 / 40, h - w);
			}
			final int tw = text.getMeasuredWidth();
			text.layout(w / 2 - tw / 2, h / 10, w / 2 + tw / 2, h / 10 + text.getMeasuredHeight());
		} else {
			if (changed) {
				wadokei.layout(w - h * 19 / 20, h / 10, w - h / 20, h);
				arrow.layout(w - h * 21 / 40, h / 20, w - h * 19 / 40, h / 10);
			}
			final int th = text.getMeasuredHeight();
			text.layout(w / 40, h / 2 - th / 2, w / 40 + text.getMeasuredWidth(), h / 2 + th / 2);
		}
	}

	public void onStringResourcesChanged(final int changes) {
		text.setText(vertical ? sr.getHrOf(hour.num) : sr.getHour(hour.num));
	}
}
