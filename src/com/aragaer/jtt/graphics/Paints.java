package com.aragaer.jtt.graphics;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Paint;

import com.aragaer.jtt.resources.ColorResources;
import com.aragaer.jtt.resources.ColorResources.ColorResourceChangeListener;
import com.aragaer.jtt.resources.RuntimeResources;

public class Paints implements ColorResourceChangeListener {
	private static Paints app_paints, widget_paints;
	private final ColorResources cr;
	public final Paint
			glyph_stroke = new Paint(0x07),
			wadokei_stroke = new Paint(0x07),

			day_fill = new Paint(0x01),
			glyph_fill = new Paint(0x01),
			night_fill = new Paint(0x01),
			wadokei_fill = new Paint(0x01);
	private final boolean is_widget;

	interface PaintChangeListener {
		public void onPaintChanged();
	}

	private final List<PaintChangeListener> listeners = new ArrayList<PaintChangeListener>();

	public Paints(Context ctx, boolean is_widget) {
		this.is_widget = is_widget;
		cr = RuntimeResources.get(ctx).getInstance(ColorResources.class);
		cr.registerStringResourceChangeListener(this,
				is_widget
					? ColorResources.TYPE_WIDGET_STYLE
					: ColorResources.TYPE_APP_THEME);

		glyph_stroke.setStyle(Paint.Style.STROKE);
		wadokei_stroke.setStyle(Paint.Style.STROKE);

		day_fill.setStyle(Paint.Style.FILL);
		glyph_fill.setStyle(Paint.Style.FILL);
		night_fill.setStyle(Paint.Style.FILL);
		wadokei_fill.setStyle(Paint.Style.FILL);

		glyph_stroke.setTextAlign(Paint.Align.CENTER);
		wadokei_stroke.setTextAlign(Paint.Align.CENTER);

		day_fill.setTextAlign(Paint.Align.CENTER);
		glyph_fill.setTextAlign(Paint.Align.CENTER);
		night_fill.setTextAlign(Paint.Align.CENTER);
		wadokei_fill.setTextAlign(Paint.Align.CENTER);

		onColorResourcesChanged(0);
	}

	public static Paints forApplication(Context ctx) {
		if (app_paints == null)
			app_paints = new Paints(ctx, false);
		return app_paints;
	}

	public static Paints forWidget(Context ctx) {
		if (widget_paints == null)
			widget_paints = new Paints(ctx, true);
		return widget_paints;
	}

	public void onColorResourcesChanged(int changes) {
		glyph_stroke.setColor(cr.getGlyphStroke(is_widget));
		wadokei_stroke.setColor(cr.getWadokeiStroke(is_widget));

		day_fill.setColor(cr.getDayFill(is_widget));
		glyph_fill.setColor(cr.getGlyphFill(is_widget));
		night_fill.setColor(cr.getNightFill(is_widget));
		wadokei_fill.setColor(cr.getWadokeiFill(is_widget));

		notifyChange();
	}

	public synchronized void registerPaintChangeListener(
			final PaintChangeListener listener) {
		listeners.add(listener);
	}

	public synchronized void unregisterStringResourceChangeListener(
			final PaintChangeListener listener) {
		listeners.remove(listener);
	}

	private synchronized void notifyChange() {
		for (PaintChangeListener listener : listeners)
			listener.onPaintChanged();
	}
}
