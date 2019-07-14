package com.aragaer.jtt.graphics;

import com.aragaer.jtt.core.Hour;

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
	private final Paint cache_paint = new Paint(0x07);
	private final Matrix matrix = new Matrix();

	private final Paints paints;
	private Bitmap clock = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444), // make it non-null
			sun_stages = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444),
			glyphs = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);

	public WadokeiDraw(Paints paints) {
		this.paints = paints;
	}

	private int unit;

	public void setUnit(final int new_unit) {
		unit = new_unit;

		paints.glyph_stroke.setTextSize(unit * 3 / 2f);
		paints.glyph_fill.setTextSize(unit * 3 / 2f);

		prepare_sun_stages();
		prepare_dial();
		glyphs.recycle();
		glyphs = Bitmap.createBitmap(unit * 16, unit * 16, Bitmap.Config.ARGB_4444);
	}

	private final static int arc_start = -90 - Math.round(step / 2f - gap);
	private final static int arc_end = -90 + Math.round(step / 2f - gap);
	private final static int arc_len = arc_end - arc_start;

	private void prepare_sun_stages() {
		final RectF sun = new RectF(0, 0, unit * 2.5f, unit * 2.5f);
		sun.inset(2, 2);
		int iR = unit * 7 / 2;

		sun_stages.recycle();
		sun_stages = Bitmap.createBitmap(unit * 7, unit * 7, Bitmap.Config.ARGB_4444);

		final Canvas canvas = new Canvas(sun_stages);

		canvas.drawArc(sun, 0, 360, false, paints.day_fill);
		canvas.drawArc(sun, 0, 360, false, paints.wadokei_stroke);
		canvas.rotate(90, iR, iR);
		canvas.rotate(45, sun.centerX(), sun.centerY());
		canvas.drawArc(sun, 0, 180, false, paints.day_fill);
		canvas.drawArc(sun, 180, 180, false, paints.night_fill);
		canvas.drawArc(sun, 0, 360, false, paints.wadokei_stroke);
		canvas.drawLine(sun.left, sun.centerY(), sun.right, sun.centerY(), paints.wadokei_stroke);
		canvas.rotate(-45, sun.centerX(), sun.centerY());
		canvas.rotate(90, iR, iR);
		canvas.drawArc(sun, 0, 360, false, paints.night_fill);
		canvas.drawArc(sun, 0, 360, false, paints.wadokei_stroke);
		canvas.rotate(90, iR, iR);
		canvas.rotate(45, sun.centerX(), sun.centerY());
		canvas.drawArc(sun, 180, 180, false, paints.day_fill);
		canvas.drawArc(sun, 0, 180, false, paints.night_fill);
		canvas.drawArc(sun, 0, 360, false, paints.wadokei_stroke);
		canvas.drawLine(sun.left, sun.centerY(), sun.right, sun.centerY(), paints.wadokei_stroke);
		canvas.rotate(-45, sun.centerX(), sun.centerY());
	}

	private void prepare_dial() {
		// with extra couple pixels on the edge
		final RectF outer = new RectF(0, 0, unit * 18, unit * 18),
				inner = new RectF(0, 0, unit * 9, unit * 9),
				sel = new RectF(0, 0, unit * 20, unit * 20);
		outer.inset(2, 2);
		outer.offset(0, unit);
		inner.offset(unit * 4.5f, unit * 5.5f);
		sel.inset(2, 2);
		sel.offset(-unit, 0);
		final Path path = new Path();

		clock.recycle();
		clock = Bitmap.createBitmap(unit * 18, unit * 19, Bitmap.Config.ARGB_4444);

		final Canvas canvas = new Canvas(clock);

		path.reset();
		path.addArc(inner, arc_start, arc_len);
		path.arcTo(sel, arc_end, -arc_len);
		path.close();

		canvas.drawPath(path, paints.wadokei_fill);
		canvas.drawPath(path, paints.wadokei_stroke);

		path.reset();
		path.addArc(inner, arc_start, arc_len);
		path.arcTo(outer, arc_end, -arc_len);
		path.close();

		for (int hr = 1; hr < 12; hr++) {
			canvas.rotate(step, unit * 9, unit * 10);
			canvas.drawPath(path, paints.wadokei_fill);
			canvas.drawPath(path, paints.wadokei_stroke);
		}
	}

	public void prepare_glyphs(final int num) {
		final Canvas canvas = new Canvas(glyphs);
		canvas.drawColor(0, Mode.CLEAR);
		final int center = glyphs.getWidth() / 2;
		// magic numbers here!
		final int y = unit * 2;
		final int y_s = unit * 3 / 2;

		for (int hr = 0; hr < 12; hr++) {
			final int glyph_y = hr == num ? y_s : y;
			canvas.drawText(Hour.Glyphs[hr], center, glyph_y, paints.glyph_fill);
			canvas.drawText(Hour.Glyphs[hr], center, glyph_y, paints.glyph_stroke);
			canvas.rotate(step, center, center);
		}
	}

	private static final float QUARTER_ANGLE = (step - gap * 2) / Hour.QUARTERS,
			PART_ANGLE = QUARTER_ANGLE / Hour.TICKS_PER_QUARTER;

	public void draw_dial(final Hour hour, final Canvas canvas) {
		final float clock_angle = step / 2f - gap
				- QUARTER_ANGLE * hour.quarter
				- PART_ANGLE * hour.tick;
		final float angle = clock_angle - hour.num * step;

		matrix.setRotate(clock_angle, unit * 9, unit * 10);
		canvas.drawBitmap(clock, matrix, cache_paint);

		matrix.setTranslate(unit * 5.5f, unit * 6.5f);
		matrix.preRotate(angle - 45, unit * 7 / 2f, unit * 7 / 2f);
		canvas.drawBitmap(sun_stages, matrix, cache_paint);

		matrix.setTranslate(unit, unit * 2);
		matrix.preRotate(angle, unit * 8, unit * 8);
		canvas.drawBitmap(glyphs, matrix, cache_paint);
	}

	public void release() {
		clock.recycle();
		glyphs.recycle();
		sun_stages.recycle();

		clock = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		sun_stages = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		glyphs = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
	}
}
