package com.aragaer.jtt;

import com.aragaer.jtt.graphics.ArrowView;
import com.aragaer.jtt.graphics.WadokeiView;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

public class ClockView extends ViewGroup {
	private int hn = -1, hf;
	private final JTTUtil.StringsHelper hs;
	private final TextView text;
	private final WadokeiView wadokei;
	private final ArrowView arrow;
	private boolean vertical;

	public ClockView(Context context) {
		super(context);
		hs = JTTUtil.getStringsHelper(context);
		wadokei = new WadokeiView(context);
		arrow = new ArrowView(context);
		text = new TextView(context);

		text.setTextColor(Color.WHITE);
		text.setGravity(Gravity.CENTER);

		addView(wadokei);
		addView(arrow);
		addView(text);
	}

	private static final int granularity = 10;
	public void setHour(int n, int f) {
		f -= f % granularity;
		if (hn == n && hf == f)
			return; // do nothing
		hn = n;
		wadokei.set_hour(n, f);
		text.setText(vertical ? hs.getHrOf(n) : hs.getHour(n));
	}

	protected void onMeasure(int wms, int hms) {
		final int w = MeasureSpec.getSize(wms);
		final int h = MeasureSpec.getSize(hms);
		vertical = h > w;
		text.setTextSize(vertical ? w / 20 : w / 15);
		text.measure(0, 0);
		setMeasuredDimension(w, h);
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int w = r - l;
		final int h = b - t;
		if (vertical) {
			if (changed) {
				wadokei.layout(0, h - w * 19 / 20, w, h);
				arrow.layout(w * 19 / 40, h - w, w * 21 / 40, h - w * 19 / 20);
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
}
