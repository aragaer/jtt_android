package com.aragaer.jtt.wadokei;

import android.test.AndroidTestCase;
import android.widget.TextView;

public class AutoresizeTextViewTest extends AndroidTestCase {
	public void testChangeTextSize() {
		TextView view = new AutoresizeTextView(getContext());
		view.setTextSize(42);
		view.setText("test");
		view.measure(100, 0);
		assertTrue(view.getPaint().measureText("test") <= 100);
	}
}