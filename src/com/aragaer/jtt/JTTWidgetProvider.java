package com.aragaer.jtt;

import java.util.Map;
import java.util.HashMap;

import com.aragaer.jtt.core.Clockwork;
import com.aragaer.jtt.core.Hour;
import com.aragaer.jtt.graphics.Paints;
import com.aragaer.jtt.graphics.WadokeiDraw;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

interface WidgetPainter {
	void hour_changed(final int n);
	void fill_rv(final RemoteViews rv, final Hour h);
	void init(final Context c);
}

public class JTTWidgetProvider {
	private static final String PKG_NAME = "com.aragaer.jtt";

	private static final class WidgetHolder {
		final ComponentName cn;
		Hour last_update;
		final WidgetPainter painter;
		final int granularity;

		WidgetHolder(final Class cls, final WidgetPainter painter, int granularity) {
			cn = new ComponentName(PKG_NAME, cls.getName());
			this.painter = painter;
			this.granularity = granularity;
		}
	}

	static private final Map<Class<?>, WidgetHolder> classes = new HashMap<Class<?>, WidgetHolder>();
	static void draw_all(final Context c) {
		for (WidgetHolder holder : classes.values())
			draw(c, null, holder);
	}

	private static abstract class JTTWidget extends AppWidgetProvider {
		protected JTTWidget(final int frequency, final Class<? extends WidgetPainter> painter_class) {
			int granularity = Hour.QUARTERS * Hour.QUARTER_PARTS / frequency;

			final Class<?> cls = getClass();
			if (!classes.containsKey(cls))
				try {
					classes.put(cls, new WidgetHolder(cls, painter_class.newInstance(), granularity));
				} catch (IllegalAccessException e) {
					Log.e("JTT WIDGET", "Failed to instantiate painter", e);
				} catch (InstantiationException e) {
					Log.e("JTT WIDGET", "Failed to instantiate painter", e);
				}
		}

		public void onReceive(Context c, Intent i) {
			final String action = i.getAction();
			classes.get(getClass()).painter.init(c);
			if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE))
				update(c, i);
			else if (action.equals(Clockwork.ACTION_JTT_TICK))
				tick(c, i, getClass());
			else
				Log.d("Widgets", "Got action "+action);
		}

		private void update(Context c, Intent i) {
			int[] ids = i.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
			draw(c, ids, classes.get(getClass()));
		}
	}

	private static void tick(Context c, Intent i, Class<?> cls) {
		int n = i.getIntExtra("hour", 0);
		int wrapped = i.getIntExtra("jtt", 0);
		final WidgetHolder holder = classes.get(cls);
		Hour prev = holder.last_update;
		if (prev == null) {
			wrapped -= wrapped % holder.granularity;
			holder.last_update = prev = Hour.fromWrapped(wrapped, null);
			holder.painter.hour_changed(n);
		} else {
			if (prev.num != n)
				holder.painter.hour_changed(n);
			if (!prev.compareAndUpdate(wrapped, holder.granularity))
				return; // do nothing
		}
		draw(c, null, holder);
	}

	private static void draw(Context c, int[] ids, final WidgetHolder holder) {
		final AppWidgetManager awm = AppWidgetManager.getInstance(c.getApplicationContext());
		if (ids == null)
			ids = awm.getAppWidgetIds(holder.cn);
		if (ids.length == 0)
			return;

		RemoteViews rv;
		if (holder.last_update == null)
			rv = new RemoteViews(PKG_NAME, R.layout.widget_loading);
		else {
			boolean inverse = PreferenceManager.getDefaultSharedPreferences(c)
					.getBoolean(Settings.PREF_WIDGET_INVERSE, false);
			rv = new RemoteViews(PKG_NAME, inverse ? R.layout.widget_inverse : R.layout.widget);
			holder.painter.fill_rv(rv, holder.last_update);
		}
		PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, new Intent(c, JTTMainActivity.class), 0);
		rv.setOnClickPendingIntent(R.id.clock, pendingIntent);

		for (int id : ids)
			awm.updateAppWidget(id, rv);
	}

	/* Widget showing only 1 hour */
	public static class Widget1 extends JTTWidget {
		public Widget1() {
			super(20, WidgetPainter1.class);
		}
	}

	/* Widget showing 12 hours */
	public static class Widget12 extends JTTWidget {
		public Widget12() {
			super(8, WidgetPainter12.class);
		}
	}
}

class WidgetPainter1 implements WidgetPainter {
	private static Paints paints;
	private static Bitmap bmp;
	private static final Canvas c = new Canvas();
	private static final Path path1 = new Path(), path2 = new Path();
	private static final RectF outer = new RectF(), inner = new RectF();

	public void hour_changed(final int n) { }

	private static final float QUARTER_ANGLE = 355f / Hour.QUARTERS,
			PART_ANGLE = QUARTER_ANGLE / Hour.QUARTER_PARTS;
	public void fill_rv(final RemoteViews rv, final Hour h) {
		bmp.eraseColor(Color.TRANSPARENT);

		final float angle = 2.5f + QUARTER_ANGLE * h.quarter + PART_ANGLE * h.quarter_parts;

		path1.rewind();
		path1.arcTo(inner, angle - 90, -angle);
		path1.arcTo(outer, -90, angle);

		path2.rewind();
		path2.arcTo(outer, -90, angle - 360);
		path2.arcTo(inner, angle - 90, 360 - angle);

		c.drawPath(path1, paints.solid1);
		c.drawPath(path2, paints.solid2);
		c.drawPath(path1, paints.stroke1);
		c.drawPath(path2, paints.stroke1);

		rv.setImageViewBitmap(R.id.clock, bmp);
		rv.setTextViewText(R.id.glyph, Hour.Glyphs[h.num]);
	}

	public void init(final Context ctx) {
		if (paints != null)
			return;

		paints = Paints.getInstance(ctx);
		final float scale = ctx.getResources().getDisplayMetrics().density;
		bmp = Bitmap.createBitmap((int) (80 * scale), (int) (80 * scale), Bitmap.Config.ARGB_4444);
		c.setBitmap(bmp);
		outer.set(3 * scale, 3 * scale, 77 * scale, 77 * scale);
		inner.set(15 * scale, 15 * scale, 65 * scale, 65 * scale);
	}
}

class WidgetPainter12 implements WidgetPainter {
	static StringResources sr;
	static WadokeiDraw wd;
	static int size;
	private static Bitmap bmp;

	public void hour_changed(final int n) {
		wd.prepare_glyphs(n);
	}

	public void fill_rv(final RemoteViews rv, final Hour h) {
		bmp.eraseColor(Color.TRANSPARENT);
		wd.draw_dial(h, new Canvas(bmp));

		rv.setImageViewBitmap(R.id.clock, bmp);
		rv.setFloat(R.id.glyph, "setTextSize", size / 10);
		rv.setTextViewText(R.id.glyph, sr.getHour(h.num));
	}

	public void init(final Context c) {
		if (sr != null)
			return;
		sr = RuntimeResources.get(c).getInstance(StringResources.class);
		wd = new WadokeiDraw(c);
		size = Math.round(110 * c.getResources().getDisplayMetrics().density);

		bmp = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_4444);
		wd.set_dial_size(size);
	}
}
