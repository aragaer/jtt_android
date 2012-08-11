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
import android.widget.RemoteViews;

public class JTTWidgetProvider {
    static class IncomingHandler extends Handler {
        private final Context ctx;
        public IncomingHandler(Context c) {
            super();
            ctx = c;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case JTTService.MSG_HOUR:
            case JTTService.MSG_SUBTICK:
                Widget1.onTick(ctx, msg.arg1, msg.arg2);
                Widget12.onTick(ctx, msg.arg1, msg.arg2);
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

    /* Widget showing only 1 hour */
    public static class Widget1 extends AppWidgetProvider {
        private static final ComponentName JTT_WIDGET1 = new ComponentName("com.aragaer.jtt", "com.aragaer.jtt.JTTWidgetProvider$Widget1");
        static AppWidgetManager awm = null;

        static Bitmap drawProgress(Context ctx, int f) {
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setFilterBitmap(true);
            p.setDither(true);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.TRANSPARENT);
            p.setShadowLayer(10f, 0, 0, Color.parseColor(ctx.getString(R.color.night)));

            Bitmap result = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(result);
            final Path path = new Path();

            path.addCircle(40, 40, 20, Direction.CW);
            path.addCircle(40, 40, 30, Direction.CCW);
            c.drawPath(path, p);
            path.reset();
            
            p.setColor(Color.parseColor(ctx.getString(R.color.fill)));
            p.clearShadowLayer();
            p.setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFCCCCCC));

            path.addArc(new RectF(20, 20, 60, 60), f * 3.6f - 90, -f * 3.6f);
            path.arcTo(new RectF(10, 10, 70, 70), -90, f * 3.6f);
            c.drawPath(path, p);
            return result;
        }

        static private void onTick(Context ctx, int n, int f) {
            if (awm == null)
                awm = AppWidgetManager.getInstance(ctx.getApplicationContext());

            final int[] ids = awm.getAppWidgetIds(JTT_WIDGET1);
            if (ids.length == 0)
                return;

            Intent intent = new Intent(ctx, JTTMainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
            Bitmap bmp = drawProgress(ctx, f);

            RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.widget);
            rv.setOnClickPendingIntent(R.id.clock, pendingIntent);
            rv.setImageViewBitmap(R.id.clock, bmp);
            rv.setTextViewText(R.id.glyph, JTTHour.Glyphs[n]);

            for (int id : ids)
                awm.updateAppWidget(id, rv);
        }

        public void onUpdate(Context ctx, AppWidgetManager awm, int[] ids) {
            onTick(ctx, 0, 0);
        }

        public void onEnabled(Context ctx) {
            onTick(ctx, 0, 0);
        }
    }

    /* Widget showing 12 hours */
    public static class Widget12 extends AppWidgetProvider {
        private static final ComponentName JTT_WIDGET12 = new ComponentName("com.aragaer.jtt", "com.aragaer.jtt.JTTWidgetProvider$Widget12");
        static AppWidgetManager awm = null;
        static JTTHour.StringsHelper hs = null;
        static JTTClockView jcv = null;
        static int size = 0;
        private final static int step = 360 / 12;
        private final static float gap = 1.5f;

        static Bitmap drawRotatedBitmap(int n, int f, String text) {
            jcv.solid1.setTextSize(size / 5);
            jcv.stroke1.setTextSize(size / 5);

            Bitmap result = Bitmap.createBitmap(size * 2, size * 2,
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            final Matrix m = new Matrix();
            m.preRotate(step * (0.5f - n) - gap - (step - gap * 2)
                    * f / 100.0f, size, size);
            if (jcv.clock.isRecycled())
                jcv.clock = jcv.drawBitmap(n, size);
            canvas.drawBitmap(jcv.clock, m, jcv.stroke2);
            canvas.drawText("▽", size, size / 8, jcv.stroke1);
            if (text != null) {
                Paint p = new Paint(jcv.stroke1);
                p.setTextSize(size / 4);
                p.setShadowLayer(4, 0, 0, Color.WHITE);
                canvas.drawText(text, size, size + size / 60, p);
            }
            canvas.drawText("▼", size, size / 8, jcv.solid1);

            return result;
        }

        static int prev_h = -1, prev_f = 0;
        static private void onTick(Context ctx, int n, int f) {
            if (awm == null) {
                awm = AppWidgetManager.getInstance(ctx.getApplicationContext());
                hs = new JTTHour.StringsHelper(ctx);
                jcv = new JTTClockView(ctx);
                size = Math.round(110 * ctx.getResources().getDisplayMetrics().density);
            }

            if (prev_h != n) {
                prev_h = n;
                jcv.clock.recycle();
            }

            final int[] ids = awm.getAppWidgetIds(JTT_WIDGET12);
            if (ids.length == 0)
                return;

            Intent intent = new Intent(ctx, JTTMainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);

            prev_f = f;
            Bitmap bmp = drawRotatedBitmap(n, f, hs.getHour(n)); 

            RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.widget);
            rv.setOnClickPendingIntent(R.id.clock, pendingIntent);
            rv.setImageViewBitmap(R.id.clock, bmp);

            for (int id : ids)
                awm.updateAppWidget(id, rv);
        }

        public void onUpdate(Context ctx, AppWidgetManager awm, int[] ids) {
            onTick(ctx, prev_h >= 0 ? prev_h : 0, prev_f);
        }

        public void onEnabled(Context ctx) {
            onTick(ctx, prev_h >= 0 ? prev_h : 0, prev_f);
        }
    }
}
