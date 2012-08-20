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
import android.graphics.Path.Direction;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

public class JTTWidgetProvider {
    static class IncomingHandler extends Handler {
        private final Widget1 w1 = new Widget1();
        private final Widget12 w12 = new Widget12();

        public IncomingHandler(Context c) {
            super();
            w1.onEnabled(c);
            w12.onEnabled(c);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case JTTService.MSG_HOUR:
            case JTTService.MSG_SUBTICK:
                w1.onTick(msg.arg1, msg.arg2, null);
                w12.onTick(msg.arg1, msg.arg2, null);
                break;
            case JTTService.MSG_TRANSITIONS:
            case JTTService.MSG_INVALIDATE:
                break;
            default:
                super.handleMessage(msg);
                break;
            }
        }
    }

    public static Handler widgetsMessenger(Context c) {
        return new IncomingHandler(c);
    }

    private static abstract class JTTWidget extends AppWidgetProvider {
        private static final String PKG_NAME = "com.aragaer.jtt";
        private final int granularity;
        private AppWidgetManager awm = null;
        final private ComponentName name;
        private int prev_h = -1, prev_f = 0;
        Context ctx = null;

        protected void onTick(int n, int f, int ids[]) {
            if (awm == null) {
                try {
                    awm = AppWidgetManager.getInstance(ctx.getApplicationContext());
                    init();
                } catch (NullPointerException e) {
                    Log.d("Widgets", "Not initialized yet");
                    return;
                }
            }

            f -= f % granularity;

            if (prev_h != n) {
                prev_h = n;
                hourChanged();
            } else if (prev_f == f && ids != null)
                return; // do nothing
            prev_f = f;

            if (ids == null) {
                ids = awm.getAppWidgetIds(name);
                if (ids.length == 0)
                    return;
            }

            Intent intent = new Intent(ctx, JTTMainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);

            RemoteViews rv = new RemoteViews(PKG_NAME, R.layout.widget);
            rv.setOnClickPendingIntent(R.id.clock, pendingIntent);
            prepareRv(rv, n, f);

            for (int id : ids)
                awm.updateAppWidget(id, rv);
        }

        abstract protected void hourChanged();
        abstract protected void prepareRv(RemoteViews rv, int n, int f);
        abstract protected void init();

        public JTTWidget(String className, int gran) {
            name = new ComponentName(PKG_NAME, className);
            granularity = gran;
        }

        public void onUpdate(Context c, AppWidgetManager awm, int[] ids) {
            if (ctx == null)
                ctx = c;
            if (awm == null)
                this.awm = awm;
            Log.d("Widgets", "Called update for "+ids.length+" widgets "+name.flattenToString());
            onTick(prev_h >= 0 ? prev_h : 0, prev_f, ids);
        }

        public void onEnabled(Context c) {
            if (ctx == null)
                ctx = c;
            onTick(prev_h >= 0 ? prev_h : 0, prev_f, null);
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

        public Widget1() {
            super("com.aragaer.jtt.JTTWidgetProvider$Widget1", 5);

            p1.setStyle(Paint.Style.FILL);
            p1.setColor(Color.TRANSPARENT);
            p2.setStyle(Paint.Style.FILL);
            p2.setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFCCCCCC));

            path1.addCircle(40, 40, 20, Direction.CW);
            path1.addCircle(40, 40, 30, Direction.CCW);
        }

        protected void hourChanged() { }

        protected void prepareRv(RemoteViews rv, int n, int f) {
            bmp.eraseColor(Color.TRANSPARENT);

            c.drawPath(path1, p1);

            path2.reset();
            path2.addArc(inner, f * 3.6f - 90, -f * 3.6f);
            path2.arcTo(outer, -90, f * 3.6f);
            c.drawPath(path2, p2);

            rv.setImageViewBitmap(R.id.clock, bmp);
            rv.setTextViewText(R.id.glyph, JTTHour.Glyphs[n]);
        }

        protected void init() {
            p1.setShadowLayer(10f, 0, 0, Color.parseColor(ctx.getString(R.color.night)));
            p2.setColor(Color.parseColor(ctx.getString(R.color.fill)));
        }
    }

    /* Widget showing 12 hours */
    public static class Widget12 extends JTTWidget {
        private static Paint p;
        JTTHour.StringsHelper hs;
        JTTClockView jcv;
        int size;
        private final static int step = 360 / 12;
        private final static float gap = 1.5f;
        final Matrix m = new Matrix();
        private Bitmap bmp;
        private Canvas canvas;

        public Widget12() {
            super("com.aragaer.jtt.JTTWidgetProvider$Widget12", 20);
        }

        protected void hourChanged() {
            jcv.clock.recycle();
        }

        @Override
        protected void prepareRv(RemoteViews rv, int n, int f) {
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

        protected void init() {
            hs = new JTTHour.StringsHelper(ctx);
            jcv = new JTTClockView(ctx);
            size = Math.round(110 * ctx.getResources().getDisplayMetrics().density);
            p = new Paint(jcv.stroke1);
            p.setTextSize(size / 4);
            p.setShadowLayer(4, 0, 0, Color.WHITE);

            bmp = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bmp);
        }
    }
}
