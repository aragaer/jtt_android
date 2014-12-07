package com.aragaer.jtt.wadokei;

import android.content.Context;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoresizeTextView extends TextView {
	public AutoresizeTextView(Context context) {
		super(context);
	}

	protected void onMeasure(int wms, int hms) {
		super.onMeasure(wms, hms);
		float measured_width = getPaint().measureText(getText().toString());
		int allowed = MeasureSpec.getSize(wms);
		if (measured_width > allowed) {
			setTextSize(TypedValue.COMPLEX_UNIT_PX, (getTextSize() * allowed)
					/ measured_width);
			super.onMeasure(wms, hms);
		}
	}
}
