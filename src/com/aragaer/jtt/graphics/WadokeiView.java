package com.aragaer.jtt.graphics;

import com.aragaer.jtt.core.Hour;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public class WadokeiView extends View {
	private final WadokeiDraw wd;
	private final Hour hour = new Hour(0);

	public WadokeiView(Context context) {
		super(context);
		wd = new WadokeiDraw(context);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w == oldw)
			return;
		wd.set_dial_size(w / 2);
		wd.prepare_glyphs(hour.num);
	}

	public void set_hour(final Hour new_hour) {
		if (hour.num != new_hour.num)
			wd.prepare_glyphs(new_hour.num);
		hour.setTo(new_hour.num, new_hour.quarter, new_hour.quarter_parts);
		invalidate();
	}

	protected void onDraw(Canvas canvas) {
		wd.draw_dial(hour, canvas);
	}
}
