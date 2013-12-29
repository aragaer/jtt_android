package com.aragaer.jtt.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.view.View;

public class ArrowView extends View {
	private final Paints paints;
	private final Path path = new Path();
	public ArrowView(Context context, Paints paints) {
		super(context);
		this.paints = paints;
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		path.reset();
		path.moveTo(0, 1);
		path.rLineTo(w, 1);
		path.rLineTo(-w / 2, h);
		path.close();
	}

	protected void onDraw(Canvas canvas) {
		canvas.drawPath(path, paints.wadokei_fill);
		canvas.drawPath(path, paints.wadokei_stroke);
	}
}
