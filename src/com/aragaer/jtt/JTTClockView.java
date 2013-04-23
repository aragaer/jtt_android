package com.aragaer.jtt;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class JTTClockView extends View {
	private final static int step = 360 / 12;
	private final static float gap = 1.5f;
	private final Paint stroke1 = new Paint(0x07),
			stroke2 = new Paint(0x07),
			solid1 = new Paint(0x01),
			solid2 = new Paint(0x01),
			cache_paint = new Paint(0x07);
	private Bitmap clock = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565); // make it non-null
	private Bitmap cache = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565); // same
	private final Canvas cache_canvas = new Canvas(), clock_canvas = new Canvas();
	private int hn = -1, hf;
	private final JTTUtil.StringsHelper hs;
	private final Matrix m = new Matrix();
	PainterTask painter = null;

	ReentrantLock cache_lock = new ReentrantLock();
	Condition need_update = cache_lock.newCondition();
	boolean update_all = true;

	public JTTClockView(Context context) {
		super(context);
		hs = JTTUtil.getStringsHelper(context);
		setupPaint(context);
	}

	int size, ox, oy, hx, hy, cox, coy;
	int iR, oR, thick, selR, clockR;
	float sR;
	boolean vertical;

	boolean circle_drawn = false;
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		vertical = h > w;
		int new_size = vertical ? w / 2 : h / 2;
		if (new_size == 0)
			return;

		cache_lock.lock();

		cache.recycle();
		cache = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		setBitmap(cache);

		stroke2.setTextSize(vertical ? w / 20 : w / 15);
		if (vertical) {
			ox = 0;
			oy = 3 * h / 5 - new_size;
			hx = new_size;
			hy = h / 10;
		} else {
			ox = 7 * w / 10 - new_size;
			oy = 0;
			hx = w / 5;
			hy = new_size;
		}

		set_dial_size(new_size);

		draw_arrow();

		clock_area.set(ox + size - oR - 2, oy + size - selR - 2, ox + size + oR + 2, oy + size + oR + 2);
		circle_drawn = false;
		draw_circle_placeholder();
		update_all = true;

		if (painter == null) {
			painter = new PainterTask();
			painter.execute();
		} else
			need_update.signal();

		cache_lock.unlock();
	}

	void draw_arrow() {
		if (!cache_lock.tryLock()) {
			Log.e("CLOCK", "Can't hold lock, will not draw");
			return;
		}
		cache_canvas.clipRect(ox + size * 19 / 20, oy - 3, ox + size * 21 / 20 + 1, oy + size - selR, Op.REPLACE);
		path.reset();
		path.moveTo(ox + size, oy + size - selR - 2);
		path.rLineTo(-size / 20, selR - size);
		path.rLineTo(size / 10, 0);
		path.close();
		cache_canvas.drawPath(path, solid1);
		cache_canvas.drawPath(path, stroke1);
		cache_lock.unlock();
		invalidate(ox + size * 19 / 20, oy - 3, ox + size * 21 / 20 + 1, oy + size - selR);
	}

	public void set_dial_size(int new_size) {
		if (size == new_size)
			return;

		if (!cache_lock.tryLock()) {
			Log.e("CLOCK", "Can't hold lock, not setting size");
			return;
		}

		size = new_size;

		iR = 2 * size / 5;
		thick = 2 * size / 5;
		oR = iR + thick;
		selR = oR + thick / 4;
		sR = iR * 0.2f;

		oy += thick / 8;
		hy += thick / 8;

		stroke1.setTextSize(thick / 3);
		solid2.setTextSize(thick / 3);

		clockR = selR + 2;
		cox = coy = size - clockR;

		clock.recycle();
		clock = Bitmap.createBitmap(clockR * 2, clockR * 2, Bitmap.Config.ARGB_8888);
		clock_canvas.setBitmap(clock);

		outer.set(size - oR, size - oR, size + oR, size + oR);
		inner.set(size - iR, size - iR, size + iR, size + iR);
		sel.set(size - selR, size - selR, size + selR, size + selR);
		sun.set(size - sR, size - sR, size + sR, size + sR);

		cache_lock.unlock();
	}

	private void draw_circle_placeholder() {
		if (circle_drawn)
			return;
		if (!cache_lock.tryLock()) {
			Log.e("CLOCK", "Can't hold lock, will not draw");
			return;
		}
		path.reset();
		path.addCircle(ox + size, oy + size, oR, Path.Direction.CW);
		path.addCircle(ox + size, oy + size, iR, Path.Direction.CCW);
		cache_canvas.clipRect(clock_area, Op.REPLACE);
		cache_canvas.drawPath(path, solid1);
		cache_canvas.drawPath(path, stroke1);
		cache_lock.unlock();
		circle_drawn = true;

		postInvalidate(clock_area.left, clock_area.top, clock_area.right, clock_area.bottom);
	}

	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(cache, 0, 0, null);
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

		solid2.setStyle(Paint.Style.FILL);
		solid2.setTextAlign(Paint.Align.CENTER);
		solid2.setColor(Color.parseColor(ctx.getString(R.color.night)));
	}

	final Path path = new Path();
	final RectF outer = new RectF(), inner = new RectF(), sel = new RectF(), sun = new RectF();

	final static int arc_start = -90 - Math.round(step / 2 - gap);
	final static int arc_end = -90 + Math.round(step / 2 - gap);
	final static int arc_len = arc_end - arc_start;

	public void prepare_dial(int num) {
		if (!cache_lock.tryLock()) {
			Log.e("CLOCK", "Can't hold lock, will not draw");
			return;
		}

		clock.eraseColor(Color.TRANSPARENT);
		clock_canvas.setMatrix(null);
		clock_canvas.translate(-cox, -coy);

		clock_canvas.translate(-iR * 0.75f, 0);
		clock_canvas.drawArc(sun, 0, 360, false, solid1);
		clock_canvas.drawArc(sun, 0, 360, false, stroke1);
		clock_canvas.rotate(90, size + iR * 0.75f, size);
		clock_canvas.drawArc(sun, 0, 180, false, solid1);
		clock_canvas.drawArc(sun, 180, 180, false, solid2);
		clock_canvas.drawArc(sun, 0, 360, false, stroke1);
		clock_canvas.drawLine(size - sR, size, size + sR, size, stroke1);
		clock_canvas.rotate(90, size + iR * 0.75f, size);
		clock_canvas.drawArc(sun, 0, 360, false, solid2);
		clock_canvas.drawArc(sun, 0, 360, false, stroke1);
		clock_canvas.rotate(90, size + iR * 0.75f, size);
		clock_canvas.drawArc(sun, 180, 180, false, solid1);
		clock_canvas.drawArc(sun, 0, 180, false, solid2);
		clock_canvas.drawArc(sun, 0, 360, false, stroke1);
		clock_canvas.drawLine(size - sR, size, size + sR, size, stroke1);
		clock_canvas.translate(iR * 0.75f, 0);
		clock_canvas.rotate(90, size, size);

		for (int hr = 0; hr < 12; hr++) {
			final boolean current = hr == num;

			path.reset();
			path.addArc(inner, arc_start, arc_len);
			path.arcTo(current ? sel : outer, arc_end, -arc_len);
			path.close();
			clock_canvas.drawPath(path, solid1);
			clock_canvas.drawPath(path, stroke1);

			final float glyph_y = size - iR - (current ? 5 : 4) * thick / 9;
			clock_canvas.drawText(JTTHour.Glyphs[hr], size, glyph_y, solid2);
			clock_canvas.drawText(JTTHour.Glyphs[hr], size, glyph_y, stroke1);
			clock_canvas.rotate(step, size, size);
		}
		cache_lock.unlock();
	}

	protected void onDetachedFromWindow() {
		if (painter == null) // we might have not been initialized
			return;
		cache_lock.lock();
		painter.cancel(false);
		need_update.signal();
		cache_lock.unlock();
	}

	private static final int granularity = JTTHour.QUARTERS * JTTHour.PARTS / 10;
	public void setHour(int n, int q, int f) {
		f += q * JTTHour.PARTS;
		f -= f % granularity;
		if (hn == n && hf == f)
			return; // do nothing

		update_all |= hn != n;
		hn = n;
		hf = f;
		cache_lock.lock();
		need_update.signal();
		cache_lock.unlock();
	}

	final Rect clock_area = new Rect();
	final Rect r = new Rect();
	private final class PainterTask extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... params) {
			cache_lock.lock();
			// if we're here, canvas is initialized
			// draw now if hour is already set
			if (hn >= 0)
				draw_everything();
			while (true) {
				try {
					need_update.await();
				} catch (InterruptedException e) {
					break;
				}

				if (isCancelled())
					break;

				draw_everything();
			}
			cache_lock.unlock();
			return null;
		}
	};

	private void draw_everything() {
		if (hn < 0) {
			Log.e("CLOCK", "Hour not set yet");
			return;
		}

		if (!cache_lock.tryLock()) {
			Log.e("CLOCK", "Can't hold lock, won't draw!");
			return;
		}

		if (update_all) {
			final String s = vertical ? hs.getHrOf(hn) : hs.getHour(hn);
			r.left = 0;
			r.right = cache_canvas.getWidth();
			r.top = hy - (int) stroke2.getTextSize();
			r.bottom = hy + (int) stroke2.getTextSize() / 2;
			cache_canvas.clipRect(r, Op.REPLACE);
			cache_canvas.drawColor(0, Mode.CLEAR);
			cache_canvas.drawText(s, hx, hy, stroke2);
			postInvalidate(r.left, r.top, r.right, r.bottom);
			draw_circle_placeholder();

			prepare_dial(hn);
			update_all = false;
		}
		cache_canvas.clipRect(clock_area, Op.REPLACE);
		draw_dial(hn, hf);
		cache_lock.unlock();

		postInvalidate(clock_area.left, clock_area.top, clock_area.right, clock_area.bottom);
	}

	private static final float total_f = JTTHour.QUARTERS * JTTHour.PARTS;
	public void draw_dial(int n, int f) {
		if (!cache_lock.tryLock()) {
			Log.e("CLOCK", "Can't hold lock, won't draw!");
			return;
		}
		m.reset();
		m.setTranslate(ox + cox, oy + coy);
		m.preRotate(step * (0.5f - n) - gap - (step - gap * 2) * f / total_f, clockR, clockR);
		cache_canvas.drawColor(0, Mode.CLEAR);
		cache_canvas.drawBitmap(clock, m, cache_paint);
		cache_lock.unlock();
	}

	public void setBitmap(Bitmap bmp) {
		if (!cache_lock.tryLock()) {
			Log.e("CLOCK", "Can't hold lock, won't replace bitmap!");
			return;
		}
		cache_canvas.setBitmap(bmp);
		cache_lock.unlock();
	}
}
