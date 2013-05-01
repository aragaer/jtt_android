package com.aragaer.jtt.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public class WadokeiView extends View {
	private final WadokeiDraw wd;
	int hour = -1, fraction;

	public WadokeiView(Context context) {
		super(context);
		wd = new WadokeiDraw(context);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w == oldw)
			return;
		wd.set_dial_size(w / 2);
		wd.prepare_glyphs(hour);
	}

	public void set_hour(int n, int f) {
		if (hour != n)
			wd.prepare_glyphs(n);
		hour = n;
		fraction = f;
		invalidate();
	}

	protected void onDraw(Canvas canvas) {
		wd.draw_dial(hour, fraction, canvas);
	}
}
