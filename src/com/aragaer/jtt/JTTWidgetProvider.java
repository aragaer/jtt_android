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
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.widget.RemoteViews;

interface WidgetPainter {
	Bitmap get_bmp(final Context c, final Hour h);
	String get_text(final Context c, final Hour h);
	int get_text_size(final Context c);
}

public class JTTWidgetProvider {
	private static final String PKG_NAME = "com.aragaer.jtt";

	private static final class WidgetHolder {
		final ComponentName cn;
		Hour last_update;
		final WidgetPainter painter;
		final int granularity;

		WidgetHolder(final Class<? extends JTTWidget> cls, final WidgetPainter painter, int granularity) {
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

			final Class<? extends JTTWidget> cls = getClass();
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
		int wrapped = i.getIntExtra("jtt", 0);
		final WidgetHolder holder = classes.get(cls);
		Hour prev = holder.last_update;
		if (prev == null) {
			wrapped -= wrapped % holder.granularity;
			holder.last_update = prev = Hour.fromWrapped(wrapped, null);
		} else if (!prev.compareAndUpdate(wrapped, holder.granularity))
			return; // do nothing
		draw(c, null, holder);
	}

	private static void draw(Context c, int[] ids, final WidgetHolder holder) {
		final AppWidgetManager awm = AppWidgetManager.getInstance(c.getApplicationContext());
		if (ids == null)
			ids = awm.getAppWidgetIds(holder.cn);
		if (ids.length == 0)
			return;

		RemoteViews rv;
		Bitmap bmp = null;
		if (holder.last_update == null)
			rv = new RemoteViews(PKG_NAME, R.layout.widget_loading);
		else {
			String text = holder.painter.get_text(c, holder.last_update);
			rv = new RemoteViews(PKG_NAME, R.layout.widget);
			bmp = holder.painter.get_bmp(c, holder.last_update);
			Canvas canvas = new Canvas(bmp);

			// text_paint
			final Theme widget_theme = c.getResources().newTheme();
			int theme = Settings.getWidgetTheme(c);
			widget_theme.applyStyle(theme, true);
			final Paint text_paint = new Paint(0x07);
			text_paint.setTextAlign(Paint.Align.CENTER);
			TypedArray ta = widget_theme.obtainStyledAttributes(null, R.styleable.Widget, 0, 0);
			text_paint.setColor(ta.getColor(R.styleable.Widget_text_color, 0));
			ta.recycle();
			ta = widget_theme.obtainStyledAttributes(null, R.styleable.Wadokei, 0, 0);
			text_paint.setShadowLayer(3, 0, 0, ta.getColor(R.styleable.Wadokei_wadokei_stroke, 0));
			ta.recycle();

			text_paint.setTextSize(holder.painter.get_text_size(c) * c.getResources().getDisplayMetrics().density);

			canvas.drawText(text, canvas.getWidth() / 2, (canvas.getHeight() - text_paint.ascent() - text_paint.descent()) / 2, text_paint);
			rv.setImageViewBitmap(R.id.clock, bmp);
		}
		PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, new Intent(c, JTTMainActivity.class), 0);
		rv.setOnClickPendingIntent(R.id.clock, pendingIntent);

		for (int id : ids)
			awm.updateAppWidget(id, rv);
		if (bmp != null)
			bmp.recycle();
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
	private static final float QUARTER_ANGLE = 355f / Hour.QUARTERS,
			PART_ANGLE = QUARTER_ANGLE / Hour.QUARTER_PARTS;

	@Override
	public Bitmap get_bmp(Context context, Hour h) {
		int theme = Settings.getWidgetTheme(context);
		Paints paints = new Paints(context, theme);
		final float scale = context.getResources().getDisplayMetrics().density;
		Bitmap bmp = Bitmap.createBitmap((int) (80 * scale), (int) (80 * scale), Bitmap.Config.ARGB_4444);
		Canvas c = new Canvas(bmp);
		RectF outer = new RectF(3 * scale, 3 * scale, 77 * scale, 77 * scale);
		RectF inner = new RectF(15 * scale, 15 * scale, 65 * scale, 65 * scale);

		final Theme widget_theme = context.getResources().newTheme();
		widget_theme.applyStyle(theme, true);
		final Paint background = new Paint(0x01);
		TypedArray ta = widget_theme.obtainStyledAttributes(null, R.styleable.Widget, 0, 0);
		background.setColor(ta.getColor(R.styleable.Widget_widget_background, 0));
		ta.recycle();

		c.drawArc(outer, 0, 360, false, background);
		final float angle = 2.5f + QUARTER_ANGLE * h.quarter + PART_ANGLE * h.quarter_parts;

		Path path1 = new Path();
		path1.arcTo(inner, angle - 90, -angle);
		path1.arcTo(outer, -90, angle);

		Path path2 = new Path();
		path2.arcTo(outer, -90, angle - 360);
		path2.arcTo(inner, angle - 90, 360 - angle);

		c.drawPath(path1, paints.day_fill);
		c.drawPath(path2, paints.night_fill);
		c.drawPath(path1, paints.wadokei_stroke);
		c.drawPath(path2, paints.wadokei_stroke);

		return bmp;
	}

	@Override
	public String get_text(Context c, Hour h) {
		return Hour.Glyphs[h.num];
	}

	@Override
	public int get_text_size(Context c) {
		return 40;
	}
}

class WidgetPainter12 implements WidgetPainter {

	@Override
	public Bitmap get_bmp(Context c, Hour h) {
		int theme = Settings.getWidgetTheme(c);
		final Paints paints = new Paints(c, theme);
		WadokeiDraw wd = new WadokeiDraw(paints);
		int unit = Math.round(11 * c.getResources().getDisplayMetrics().density);

		final Theme widget_theme = c.getResources().newTheme();
		widget_theme.applyStyle(theme, true);
		final Paint background = new Paint(0x01);
		TypedArray ta = widget_theme.obtainStyledAttributes(null, R.styleable.Widget, 0, 0);
		background.setColor(ta.getColor(R.styleable.Widget_widget_background, 0));
		ta.recycle();

		Bitmap bmp = Bitmap.createBitmap(unit * 18, unit * 19, Bitmap.Config.ARGB_4444);
		new Canvas(bmp).drawCircle(unit * 9, unit * 10, unit * 9, background);
		wd.setUnit(unit);
		wd.prepare_glyphs(h.num);
		wd.draw_dial(h, new Canvas(bmp));
		wd.release();
		return bmp;
	}

	@Override
	public String get_text(Context c, Hour h) {
		return RuntimeResources.get(c).getInstance(StringResources.class).getHour(h.num);
	}

	@Override
	public int get_text_size(Context c) {
		return 33;
	}
}
