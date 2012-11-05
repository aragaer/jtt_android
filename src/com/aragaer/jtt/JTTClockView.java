package com.aragaer.jtt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
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
	private JTTHour hour = new JTTHour(-1); // something impossible
	private final JTTUtil.StringsHelper hs;
	private final Matrix m = new Matrix();

	public JTTClockView(Context context) {
		this(context, null, 0);
	}

	public JTTClockView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public JTTClockView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		hs = JTTUtil.getStringsHelper(ctx);
		setupPaint(ctx);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (hour.num == -1)
			return;

		final int w = getWidth();
		final int h = getHeight();
		final boolean v = h > w;
		final int size = v ? w / 2 : h / 2;

		m.reset();
		m.setTranslate(v ? 0 : 3 * w / 5 - size, v ? 3 * h / 5 - size : 0);
		m.preRotate(step * (0.5f - hour.num) - gap - (step - gap * 2)
				* hour.fraction / 100.0f, size, size);
		canvas.drawBitmap(clock, m, stroke1);

		stroke2.setTextSize(v ? w / 20 : w / 15);
		solid1.setTextSize(size / 5);
		stroke1.setTextSize(size / 5);

		final String s = v ? hs.getHrOf(hour.num) : hs.getHour(hour.num);
		canvas.drawText(s, v ? size : w / 5, v ? h / 10 : size, stroke2);
		canvas.drawText("▽", v ? size : 3 * w / 5, v ? 3 * h / 5 - 7 * size / 8 : size / 8, stroke1);
		canvas.drawText("▼", v ? size : 3 * w / 5, v ? 3 * h / 5 - 7 * size / 8 : size / 8, solid1);
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

		final int iR = 2 * c / 5;
		final int thick = 2 * c / 5;
		final int oR = iR + thick;
		final int selR = oR + thick / 4;
		final float sR = iR * 0.2f;

		stroke1.setTextSize(thick / 3);
		solid2.setTextSize(thick / 3);

		outer.set(c - oR, c - oR, c + oR, c + oR);
		inner.set(c - iR, c - iR, c + iR, c + iR);
		sel.set(c - selR, c - selR, c + selR, c + selR);
		sun.set(c - sR, c - sR, c + sR, c + sR);

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

	class PainterTask extends AsyncTask<Integer, Void, Void> {
		protected Void doInBackground(Integer... params) {
			final int n = params[0], f = params[1];
			final int w = getWidth();
			final int h = getHeight();
			final boolean v = h > w;
			final int size = v ? w / 2 : h / 2;

			if (hour.num != n)
				if (clock.getWidth() == w && clock.getHeight() == h) {
					clock.eraseColor(Color.TRANSPARENT);
					draw_onto(clock, n, size);
				} else {
					clock.recycle();
					clock = drawBitmap(n, size);
				}

			hour.setTo(n, f);
			return null;
		}

		protected void onPostExecute(Void result) {
			postInvalidate();
		}
	}
}
