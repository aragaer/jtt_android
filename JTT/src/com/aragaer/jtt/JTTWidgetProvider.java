package com.aragaer.jtt;

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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.widget.RemoteViews;

public class JTTWidgetProvider {
    private static final String PKG_NAME = "com.aragaer.jtt";

    private static abstract class JTTWidget extends AppWidgetProvider {
        final private ComponentName name;
        final private int granularity;

        abstract protected void invalidate();
        abstract protected void fill_rv(RemoteViews rv);
        abstract protected void init(Context c);
        abstract protected JTTHour get_h();
        abstract protected void set_h(JTTHour h);

        public JTTWidget(String className, int g) {
            name = new ComponentName(PKG_NAME, className);
            granularity = g;
        }

        public void onReceive(Context c, Intent i) {
            final String action = i.getAction();
            init(c);
            if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE))
                update(c, i);
            else if (action.equals(JTTService.TICK_ACTION))
                tick(c, i);
            else
                Log.d("Widgets", "Got action "+action);
        }

        private void tick(Context c, Intent i) {
            Log.d("Widgets", "Tick for "+this.getClass().getSimpleName());
            int n = i.getIntExtra("hour", 0);
            int f = i.getIntExtra("fraction", 0);
            f -= f % granularity;
            JTTHour prev = get_h();
            if (prev != null)
                if (prev.num != n)
                    invalidate();
                else if (prev.fraction == f)
                    return; // do nothing
            set_h(new JTTHour(n, f));
            draw(c, null);
        }

        private void update(Context c, Intent i) {
            int[] ids = i.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            Log.d("Widgets", "Update for "+(ids==null ? "all" : ids.length)+" widgets for "+this.getClass().getSimpleName());
            if (get_h() == null)
                return;
            draw(c, ids);
        }

        private void draw(Context c, int[] ids) {
            final AppWidgetManager awm = AppWidgetManager.getInstance(c.getApplicationContext());
            if (ids == null)
                ids = awm.getAppWidgetIds(name);
            if (ids.length == 0)
                return;
            Log.d("Widgets", "Draw "+(ids==null ? "all" : ids.length)+" widgets of "+this.getClass().getSimpleName());
            RemoteViews rv = new RemoteViews(PKG_NAME, R.layout.widget);
            PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, new Intent(c, JTTMainActivity.class), 0);
            rv.setOnClickPendingIntent(R.id.clock, pendingIntent);
            fill_rv(rv);

            for (int id : ids)
                awm.updateAppWidget(id, rv);
        }
    }

    /* Widget showing only 1 hour */
    public static class Widget1 extends JTTWidget {
        /* Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG == 0x07 */
        private final Paint p1 = new Paint(0x07), p2 = new Paint(p1);
        private final Bitmap bmp = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        private final Canvas c = new Canvas(bmp);
        private final Path path1 = new Path(), path2 = new Path();
        private final RectF outer = new RectF(10, 10, 70, 70), inner = new RectF(20, 20, 60, 60);
        static boolean initialized = false;

        public Widget1() {
            super("com.aragaer.jtt.JTTWidgetProvider$Widget1", 5);

            p1.setStyle(Paint.Style.FILL);
            p1.setColor(Color.TRANSPARENT);
            p2.setStyle(Paint.Style.FILL);
            p2.setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFCCCCCC));

            path1.addCircle(40, 40, 20, Path.Direction.CW);
            path1.addCircle(40, 40, 30, Path.Direction.CCW);
        }

        protected void invalidate() { }

        protected void fill_rv(RemoteViews rv) {
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

        static JTTHour h = null;
        protected JTTHour get_h() {
            return h;
        }
        protected void set_h(JTTHour nh) {
            h = nh;
        }
    }

    /* Widget showing 12 hours */
    public static class Widget12 extends JTTWidget {
        private static Paint p;
        static JTTHour.StringsHelper hs;
        static JTTClockView jcv;
        static int size;
        private final static int step = 360 / 12;
        private final static float gap = 1.5f;
        final Matrix m = new Matrix();
        private static Bitmap bmp;
        private static Canvas canvas;

        public Widget12() {
            super("com.aragaer.jtt.JTTWidgetProvider$Widget12", 20);
        }

        protected void invalidate() {
            jcv.clock.recycle();
        }

        @Override
        protected void fill_rv(RemoteViews rv) {
            final int n = h.num, f = h.fraction;
            bmp.eraseColor(Color.TRANSPARENT);
            m.reset();
            m.preRotate(step * (0.5f - n) - gap - (step - gap * 2) * f / 100.0f, size, size);
            if (jcv.clock.isRecycled())
                jcv.clock = jcv.drawBitmap(n, size);
            canvas.drawBitmap(jcv.clock, m, jcv.stroke2);

            jcv.solid1.setTextSize(size / 5);
            jcv.stroke1.setTextSize(size / 5);

            canvas.drawText("▽", size, size / 8, jcv.stroke1);
            canvas.drawText("▼", size, size / 8, jcv.solid1);
            canvas.drawText(hs.getHour(n), size, size + size / 60, p);

            rv.setImageViewBitmap(R.id.clock, bmp);
        }

        protected void init(Context c) {
            if (hs != null)
                return;
            hs = new JTTHour.StringsHelper(c);
            jcv = new JTTClockView(c);
            size = Math.round(110 * c.getResources().getDisplayMetrics().density);
            p = new Paint(jcv.stroke1);
            p.setTextSize(size / 4);
            p.setShadowLayer(4, 0, 0, Color.WHITE);

            bmp = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bmp);
        }

        static JTTHour h = null;
        protected JTTHour get_h() {
            return h;
        }
        protected void set_h(JTTHour nh) {
            h = nh;
        }
    }
}
