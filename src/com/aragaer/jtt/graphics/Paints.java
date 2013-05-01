package com.aragaer.jtt.graphics;

import com.aragaer.jtt.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

public class Paints {
	private static Paints singleton;
	public final Paint stroke1 = new Paint(0x07),
			stroke2 = new Paint(0x07),
			solid1 = new Paint(0x01),
			solid2 = new Paint(0x01);

	public Paints(Context ctx) {
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

	public static Paints getInstance(Context ctx) {
		if (singleton == null)
			singleton = new Paints(ctx);
		return singleton;
	}
}
