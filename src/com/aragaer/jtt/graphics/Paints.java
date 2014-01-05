package com.aragaer.jtt.graphics;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;

import com.aragaer.jtt.R;

public class Paints {
	public final Paint
			glyph_stroke = new Paint(0x07),
			wadokei_stroke = new Paint(0x07),

			day_fill = new Paint(0x01),
			glyph_fill = new Paint(0x01),
			night_fill = new Paint(0x01),
			wadokei_fill = new Paint(0x01);

	interface PaintChangeListener {
		public void onPaintChanged();
	}

	public Paints(Context context) {

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

		TypedArray ta = context.obtainStyledAttributes(null, R.styleable.Wadokei, 0, 0);

		glyph_stroke.setColor(ta.getColor(R.styleable.Wadokei_glyph_stroke, 0));
		wadokei_stroke.setColor(ta.getColor(R.styleable.Wadokei_wadokei_stroke, 0));

		day_fill.setColor(ta.getColor(R.styleable.Wadokei_day_fill, 0));
		glyph_fill.setColor(ta.getColor(R.styleable.Wadokei_glyph_fill, 0));
		night_fill.setColor(ta.getColor(R.styleable.Wadokei_night_fill, 0));
		wadokei_fill.setColor(ta.getColor(R.styleable.Wadokei_wadokei_fill, 0));

		ta.recycle();
	}
}
