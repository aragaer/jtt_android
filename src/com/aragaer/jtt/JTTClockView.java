package com.aragaer.jtt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.os.AsyncTask;
import android.util.FloatMath;
import android.view.View;

public class JTTClockView extends View {
	private final static int step = 360 / 12;
	private final static float gap = 1.5f;
	protected final Paint stroke1 = new Paint(0x07),
			stroke2 = new Paint(0x07),
			solid1 = new Paint(0x01),
			solid2 = new Paint(0x01);
	protected Bitmap clock = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565); // make it non-null
	private final Canvas cc = new Canvas();
	private JTTHour hour = new JTTHour(-1); // something impossible
	private final JTTUtil.StringsHelper hs;
	private final Matrix m = new Matrix();

	public JTTClockView(Context context) {
		super(context);
		hs = JTTUtil.getStringsHelper(context);
		setupPaint(context);
		setDrawingCacheEnabled(true);
	}

	int size, ox, oy, hx, hy;
	int iR, oR, thick, selR;
	float sR;
	boolean vertical;

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		vertical = h > w;
		size = vertical ? w / 2 : h / 2;
		if (size == 0)
			return;
		clock.recycle();
		clock = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888);
		cc.setBitmap(getDrawingCache());
		stroke2.setTextSize(vertical ? w / 20 : w / 15);
		solid1.setTextSize(size / 5);
		stroke1.setTextSize(size / 5);
		if (vertical) {
			ox = 0;
			oy = 3 * h / 5 - size;
			hx = size;
			hy = h / 10;
		} else {
			ox = 3 * w / 5 - size;
			oy = 0;
			hx = w / 5;
			hy = size;
		}
		cc.drawText("▽", ox + size, oy + size / 8, stroke1);
		cc.drawText("▼", ox + size, oy + size / 8, solid1);

		iR = 2 * size / 5;
		thick = 2 * size / 5;
		oR = iR + thick;
		selR = oR + thick / 4;
		sR = iR * 0.2f;

		stroke1.setTextSize(thick / 3);
		solid2.setTextSize(thick / 3);

		outer.set(size - oR, size - oR, size + oR, size + oR);
		inner.set(size - iR, size - iR, size + iR, size + iR);
		sel.set(size - selR, size - selR, size + selR, size + selR);
		sun.set(size - sR, size - sR, size + sR, size + sR);

		clock_area.set(ox + size - oR, oy + size - selR, ox + size + oR, oy + size + oR);

		cc.clipRect(ox + size - oR, oy + size - selR, ox + size + oR, oy + size + oR, Op.REPLACE);
		path.reset();
		path.addCircle(ox + size, oy + size, oR, Path.Direction.CW);
		path.addCircle(ox + size, oy + size, iR, Path.Direction.CCW);
		cc.drawPath(path, solid1);
		cc.drawPath(path, stroke1);
	}

	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(getDrawingCache(), 0, 0, null);
	}

	private final void setupPaint(Context ctx) {
		stroke1.setStyle(Paint.Style.STROKE);
		stroke1.setTextAlign(Paint.Align.CENTER);
		stroke1.setColor(Color.parseColor(ctx.getString(R.color.stroke)));

		stroke2.setTextAlign(Paint.Align.CENTER);
		stroke2.setStyle(Paint.Style.STROKE);
		stroke2.setColor(Color.WHITE);

		solid1.setStyle(Paint.Style.FILL);
		solid1.setTextAlign(Paint.Align.CENTER);
		solid1.setColor(Color.parseColor(ctx.getString(R.color.fill)));
		solid1.setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFCCCCCC));

		solid2.setStyle(Paint.Style.FILL);
		solid2.setTextAlign(Paint.Align.CENTER);
		solid2.setColor(Color.parseColor(ctx.getString(R.color.night)));
		solid2.setStrokeWidth(1.3f);
	}

	final Path path = new Path();
	final RectF outer = new RectF(), inner = new RectF(), sel = new RectF(), sun = new RectF();
	protected Bitmap drawBitmap(int num, int c) {
		Bitmap result = Bitmap.createBitmap(c * 2, c * 2,
				Bitmap.Config.ARGB_8888);
		draw_onto(result, num, c);
		return result;
	}

	void draw_onto(Bitmap b, int num, int c) {
		Canvas canvas = new Canvas(b);

		canvas.rotate(-step / 2, c, c);
		canvas.translate(-iR * 0.75f, 0);
		canvas.drawArc(sun, 0, 360, false, solid1);
		canvas.drawArc(sun, 0, 360, false, stroke1);
		canvas.rotate(90, c + iR * 0.75f, c);
		canvas.drawArc(sun, 0, 180, false, solid1);
		canvas.drawArc(sun, 180, 180, false, solid2);
		canvas.drawArc(sun, 0, 360, false, stroke1);
		canvas.drawLine(c - sR, c, c + sR, c, stroke1);
		canvas.rotate(90, c + iR * 0.75f, c);
		canvas.drawArc(sun, 0, 360, false, solid2);
		canvas.drawArc(sun, 0, 360, false, stroke1);
		canvas.rotate(90, c + iR * 0.75f, c);
		canvas.drawArc(sun, 180, 180, false, solid1);
		canvas.drawArc(sun, 0, 180, false, solid2);
		canvas.drawArc(sun, 0, 360, false, stroke1);
		canvas.drawLine(c - sR, c, c + sR, c, stroke1);
		canvas.translate(iR * 0.75f, 0);
		canvas.rotate(90 + step / 2, c, c);

		final int arc_start = -90 - Math.round(step / 2 - gap);
		final int arc_end = -90 + Math.round(step / 2 - gap);
		final int arc_len = arc_end - arc_start;

		final float start = (float) Math.toRadians(arc_start);
		final float l2x = c + FloatMath.cos(start) * iR;
		final float l2y = c + FloatMath.sin(start) * iR;

		for (int hr = 0; hr < 12; hr++) {
			path.reset();
			final boolean current = hr == num;

			path.addArc(inner, arc_start, arc_len);
			path.arcTo(current ? sel : outer, arc_end, -arc_len);
			path.lineTo(l2x, l2y);
			canvas.drawPath(path, solid1);
			canvas.drawPath(path, stroke1);

			final float glyph_y = c - iR - (current ? 5 : 4) * thick / 9;
			canvas.drawText(JTTHour.Glyphs[hr], c, glyph_y, solid2);
			canvas.drawText(JTTHour.Glyphs[hr], c, glyph_y, stroke1);
			canvas.rotate(step, c, c);
		}
	}

	private static final int granularity = 10;
	public void setHour(int n, int f) {
		f -= f % granularity;
		if (hour.num == n && hour.fraction == f)
			return; // do nothing

		new PainterTask().execute(n, f);
	}

	final Rect clock_area = new Rect();
	class PainterTask extends AsyncTask<Integer, Void, Void> {
		protected Void doInBackground(Integer... params) {
			final int n = params[0], f = params[1];

			if (hour.num != n) {
				Rect r = new Rect();
				final String s = vertical ? hs.getHrOf(n) : hs.getHour(n);
				stroke2.getTextBounds(s, 0, s.length(), r);
				r.offsetTo(-r.centerX(), r.top);
				r.offset(hx, hy);
				cc.clipRect(r, Op.REPLACE);
				cc.drawColor(0, Mode.CLEAR);
				cc.drawText(s, hx, hy, stroke2);
				postInvalidate(r.left, r.top, r.right, r.bottom);

				clock.eraseColor(Color.TRANSPARENT);
				draw_onto(clock, n, size);
			}

			cc.clipRect(clock_area, Op.REPLACE);
			cc.drawColor(0, Mode.CLEAR);
			m.reset();
			m.setTranslate(ox, oy);
			m.preRotate(step * (0.5f - n) - gap - (step - gap * 2)
					* f / 100.0f, size, size);
			cc.drawBitmap(clock, m, stroke1);
			postInvalidate(clock_area.left, clock_area.top, clock_area.right, clock_area.bottom);

			hour.setTo(n, f);
			return null;
		}
	}
}
