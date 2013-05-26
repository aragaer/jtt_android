package com.aragaer.jtt;

import java.util.HashMap;

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

public class JTTWidgetProvider {
	private static final String PKG_NAME = "com.aragaer.jtt";

	private static abstract class JTTWidget extends AppWidgetProvider {
		private final String cn;
		private final ComponentName name;
		private final int granularity;
		static private final HashMap<String, JTTHour> last_update = new HashMap<String, JTTHour>();

		abstract protected void hour_changed(int n);
		abstract protected void fill_rv(RemoteViews rv, JTTHour h);
		abstract protected void init(Context c);

		static boolean inverse;

		protected JTTWidget(int granularity) {
			cn = this.getClass().getSimpleName();
			name = new ComponentName(PKG_NAME, this.getClass().getName());
			this.granularity = granularity;
		}

		public void onReceive(Context c, Intent i) {
			final String action = i.getAction();
			init(c);
			inverse = PreferenceManager.getDefaultSharedPreferences(c)
					.getBoolean("jtt_widget_text_invert", false);
			if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE))
				update(c, i);
			else if (action.equals(JTTService.TICK_ACTION))
				tick(c, i);
			else if (action.equals(Settings.JTT_SETTINGS_CHANGED)) {
				inverse = i.getBooleanExtra("inverse", inverse);
				draw(c, null, last_update.get(cn));
			} else
				Log.d("Widgets", "Got action "+action);
		}

		private void tick(Context c, Intent i) {
			int n = i.getIntExtra("hour", 0);
			int f = i.getIntExtra("fraction", 0);
			f -= f % granularity;
			JTTHour prev = last_update.get(cn);
			int prev_n = -1;
			if (prev == null)
				// unfortunately put() returns _previous_ value, I want new
				last_update.put(cn, prev = new JTTHour(n, f));
			else
				prev_n = prev.num;
			if (prev_n != n)
				hour_changed(n);
			else if (prev.fraction == f)
				return; // do nothing
			prev.setTo(n, f);
			draw(c, null, prev);
		}

		private void update(Context c, Intent i) {
			int[] ids = i.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
			JTTHour h = last_update.get(cn);
			if (h == null)
				return;
			draw(c, ids, h);
		}

		private void draw(Context c, int[] ids, JTTHour h) {
			final AppWidgetManager awm = AppWidgetManager.getInstance(c.getApplicationContext());
			if (ids == null)
				ids = awm.getAppWidgetIds(name);
			if (ids.length == 0)
				return;

			RemoteViews rv;
			if (h == null)
				rv = new RemoteViews(PKG_NAME, R.layout.widget_loading);
			else {
				rv = new RemoteViews(PKG_NAME, inverse ? R.layout.widget_inverse : R.layout.widget);
				fill_rv(rv, h);
			}
			PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, new Intent(c, JTTMainActivity.class), 0);
			rv.setOnClickPendingIntent(R.id.clock, pendingIntent);

			for (int id : ids)
				awm.updateAppWidget(id, rv);
		}
	}

	/* Widget showing only 1 hour */
	public static class Widget1 extends JTTWidget {
		/* Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG == 0x07 */
		private final Paint p1 = new Paint(0x07), p2 = new Paint(p1);
		private final Bitmap bmp = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_4444);
		private final Canvas c = new Canvas(bmp);
		private final Path path1 = new Path(), path2 = new Path();
		private final RectF outer = new RectF(10, 10, 70, 70), inner = new RectF(20, 20, 60, 60);
		static boolean initialized = false;

		public Widget1() {
			super(5);

			p1.setStyle(Paint.Style.FILL);
			p1.setColor(Color.TRANSPARENT);
			p2.setStyle(Paint.Style.FILL);
			p2.setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFCCCCCC));

			path1.addCircle(40, 40, 20, Path.Direction.CW);
			path1.addCircle(40, 40, 30, Path.Direction.CCW);
		}

		protected void hour_changed(int n) { }

		protected void fill_rv(RemoteViews rv, JTTHour h) {
			bmp.eraseColor(Color.TRANSPARENT);

			c.drawPath(path1, p1);

			path2.reset();
			path2.addArc(inner, h.fraction * 3.6f - 90, -h.fraction * 3.6f);
			path2.arcTo(outer, -90, h.fraction * 3.6f);
			c.drawPath(path2, p2);

			rv.setImageViewBitmap(R.id.clock, bmp);
			rv.setTextViewText(R.id.glyph, JTTHour.Glyphs[h.num]);
		}

		protected void init(Context c) {
			if (initialized)
				return;

			p1.setShadowLayer(10f, 0, 0, Color.parseColor(c.getString(R.color.night)));
			p2.setColor(Color.parseColor(c.getString(R.color.fill)));
			initialized = true;
		}
	}

	/* Widget showing 12 hours */
	public static class Widget12 extends JTTWidget {
		static StringResources sr;
		static WadokeiDraw wd;
		static int size;
		private static Bitmap bmp;

		public Widget12() {
			super(12);
		}

		protected void hour_changed(int n) {
			wd.prepare_glyphs(n);
		}

		protected void fill_rv(RemoteViews rv, JTTHour h) {
			final int n = h.num, f = h.fraction;
			bmp.eraseColor(Color.TRANSPARENT);
			wd.draw_dial(n, f, new Canvas(bmp));

			rv.setImageViewBitmap(R.id.clock, bmp);
			rv.setFloat(R.id.glyph, "setTextSize", size / 10);
			rv.setTextViewText(R.id.glyph, sr.getHour(n));
		}

		protected void init(Context c) {
			if (sr != null)
				return;
			sr = RuntimeResources.get(c).getInstance(StringResources.class);
			wd = new WadokeiDraw(c);
			size = Math.round(110 * c.getResources().getDisplayMetrics().density);

			bmp = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_4444);
			wd.set_dial_size(size);
		}
	}
}
