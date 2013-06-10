package com.aragaer.jtt.graphics;

import com.aragaer.jtt.core.Hour;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;

public class WadokeiDraw {
	private final static int step = 360 / 12;
	private final static float gap = 1.5f;
	private final Paint cache_paint = new Paint(0x07), stroke, solid;
	private final Matrix clock_matrix = new Matrix(), sun_matrix = new Matrix(), glyph_matrix = new Matrix();

	private final Paints paints;
	private Bitmap clock = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444), // make it non-null
			sun_stages = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444),
			glyphs = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);

	public WadokeiDraw(Context context) {
		paints = Paints.getInstance(context);
		stroke = new Paint(paints.stroke1);
		solid = new Paint(paints.solid2);
	}

	private int size, iR;

	public void set_dial_size(final int new_size) {
		size = new_size;
		iR = size * 4 / 9;

		stroke.setTextSize(iR / 3);
		solid.setTextSize(iR / 3);

		prepare_sun_stages();
		prepare_dial();
		glyphs.recycle();
		glyphs = Bitmap.createBitmap(size * 8 / 5, size * 8 / 5, Bitmap.Config.ARGB_4444);
	}

	final static int arc_start = -90 - Math.round(step / 2 - gap);
	final static int arc_end = -90 + Math.round(step / 2 - gap);
	final static int arc_len = arc_end - arc_start;

	final private void prepare_sun_stages() {
		final RectF sun = new RectF(iR * 0.05f, iR * 0.8f, iR * 0.45f, iR * 1.2f);

		sun_stages.recycle();
		sun_stages = Bitmap.createBitmap(iR * 2, iR * 2, Bitmap.Config.ARGB_4444);

		final Canvas canvas = new Canvas(sun_stages);
		canvas.rotate(-step / 2, iR, iR);

		canvas.drawArc(sun, 0, 360, false, paints.solid1);
		canvas.drawArc(sun, 0, 360, false, paints.stroke1);
		canvas.rotate(90, iR, iR);
		canvas.drawArc(sun, 0, 180, false, paints.solid1);
		canvas.drawArc(sun, 180, 180, false, paints.solid2);
		canvas.drawArc(sun, 0, 360, false, paints.stroke1);
		canvas.drawLine(sun.left, iR, sun.right, iR, paints.stroke1);
		canvas.rotate(90, iR, iR);
		canvas.drawArc(sun, 0, 360, false, paints.solid2);
		canvas.drawArc(sun, 0, 360, false, paints.stroke1);
		canvas.rotate(90, iR, iR);
		canvas.drawArc(sun, 180, 180, false, paints.solid1);
		canvas.drawArc(sun, 0, 180, false, paints.solid2);
		canvas.drawArc(sun, 0, 360, false, paints.stroke1);
		canvas.drawLine(sun.left, iR, sun.right, iR, paints.stroke1);
	}

	public void prepare_dial() {
		final RectF outer = new RectF(size / 9, size / 9, size * 17 / 9, size * 17 / 9),
				inner = new RectF(size - iR, size - iR, size + iR, size + iR),
				sel = new RectF(2, 2, size * 2 - 2, size * 2 - 2); // extra couple pixels on the edge
		final Path path = new Path();

		clock.recycle();
		clock = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_4444);

		final Canvas canvas = new Canvas(clock);

		path.reset();
		path.addArc(inner, arc_start, arc_len);
		path.arcTo(sel, arc_end, -arc_len);
		path.close();

		canvas.drawPath(path, paints.solid1);
		canvas.drawPath(path, paints.stroke1);

		path.reset();
		path.addArc(inner, arc_start, arc_len);
		path.arcTo(outer, arc_end, -arc_len);
		path.close();

		for (int hr = 1; hr < 12; hr++) {
			canvas.rotate(step, size, size);
			canvas.drawPath(path, paints.solid1);
			canvas.drawPath(path, paints.stroke1);
		}
	}

	public void prepare_glyphs(final int num) {
		final Canvas canvas = new Canvas(glyphs);
		canvas.drawColor(0, Mode.CLEAR);
		final int center = glyphs.getWidth() / 2;
		// magic numbers here!
		final int y = size * 16 / 81;
		final int y_s = size * 10 / 81;

		for (int hr = 0; hr < 12; hr++) {
			final int glyph_y = hr == num ? y_s : y;
			canvas.drawText(Hour.Glyphs[hr], center, glyph_y, solid);
			canvas.drawText(Hour.Glyphs[hr], center, glyph_y, stroke);
			canvas.rotate(step, center, center);
		}
	}

	private static final float QUARTER_ANGLE = (step - gap * 2) / Hour.QUARTERS,
			PART_ANGLE = QUARTER_ANGLE / Hour.QUARTER_PARTS;

	public void draw_dial(final Hour hour, final Canvas canvas) {
		final float clock_angle = step / 2 - gap
				- QUARTER_ANGLE * hour.quarter
				- PART_ANGLE * hour.quarter_parts;
		final float angle = clock_angle - hour.num * step;

		clock_matrix.setRotate(clock_angle, size, size);

		sun_matrix.setTranslate(size - iR, size - iR);
		sun_matrix.preRotate(angle, iR, iR);

		glyph_matrix.setTranslate(size / 5, size / 5);
		glyph_matrix.preRotate(angle, size * 4 / 5, size * 4 / 5);

		canvas.drawBitmap(clock, clock_matrix, cache_paint);
		canvas.drawBitmap(sun_stages, sun_matrix, cache_paint);
		canvas.drawBitmap(glyphs, glyph_matrix, cache_paint);
	}
}
