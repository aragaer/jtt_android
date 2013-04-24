package com.aragaer.jtt;

import android.content.Context;
import android.content.res.Configuration;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class JTTPager extends LinearLayout implements RadioGroup.OnCheckedChangeListener {
	private PageScroller scrollview;
	private ScrollContents pageview;
	private RadioGroup tablist;
	int viewport_width;
	private RadioGroup.LayoutParams btnlp;

	public void onCheckedChanged(RadioGroup group, int checkedId) {
		scrollview.trySmoothScrollToScreen(checkedId);
	}

	public JTTPager(Context context) {
		super(context);

		scrollview = new PageScroller(context);
		pageview = new ScrollContents(context);
		scrollview.addView(pageview);

		tablist = new RadioGroup(context);
		tablist.setOnCheckedChangeListener(this);
		setPadding(5, 5, 5, 5);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setOrientation(LinearLayout.VERTICAL);
			tablist.setOrientation(LinearLayout.HORIZONTAL);
			tablist.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, 0));
			btnlp = new RadioGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.MATCH_PARENT, 1);
		} else {
			setOrientation(LinearLayout.HORIZONTAL);
			tablist.setOrientation(LinearLayout.VERTICAL);
			tablist.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.MATCH_PARENT, 0));
			btnlp = new RadioGroup.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT, 1);
		}

		addView(scrollview, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, 1));
		addView(tablist);
	}

	public int addTab(View view, int resid) {
		final int id = tablist.getChildCount();
		final RadioButton b = new RadioButton(getContext(), null);
		b.setText(getContext().getString(resid));
		b.setId(id);
		tablist.addView(b, btnlp);
		pageview.addView(view);
		if (id == 0)
			tablist.check(0);
		return id;
	}

	/**
	 * Highlight the screen name and try to scroll to it
	 * @param num - Number of screen
	 */
	protected void selectScreen(int num) {
		tablist.check(num);
	}

	class PageScroller extends HorizontalScrollView {
		int scrollFrom = -1;
		final int minfling;

		public PageScroller(Context ctx) {
			super(ctx);
			setHorizontalScrollBarEnabled(false);
			setSmoothScrollingEnabled(true);
			setHorizontalFadingEdgeEnabled(false);
			minfling = ViewConfiguration.get(ctx).getScaledMinimumFlingVelocity();
		}

		VelocityTracker vt = null;
		public boolean onTouchEvent(MotionEvent event) {
			if (vt == null)
				vt = VelocityTracker.obtain();
			vt.addMovement(event);

			if (event.getAction() == MotionEvent.ACTION_UP) {
				final int x = getScrollX();
				int targetScreen;

				vt.computeCurrentVelocity(1000);
				float velocity = vt.getXVelocity();

				/* fling gesture */
				if (Math.abs(velocity) > minfling) {
					int max = tablist.getChildCount() - 1;
					targetScreen = scrollFrom + (velocity < 0 ? 1 : -1);
					if (targetScreen < 0)
						targetScreen = 0;
					else if (targetScreen > max)
						targetScreen = max;
				} else /* return to current screen */
					targetScreen = x2n(x);

				selectScreen(targetScreen);
				smoothScrollTo(n2x(targetScreen), 0);
				scrollFrom = -1;

				vt.recycle();
				vt = null;
				return true;
			}

			if (scrollFrom == -1)
				scrollFrom = tablist.getCheckedRadioButtonId();

			return super.onTouchEvent(event);
		}

		/**
		 * Converts horizontal position to a screen number within pager
		 * @param x - Horizontal scroll position
		 * @return Screen number
		 */
		private int x2n(int x) {
			int max = tablist.getChildCount() - 1;
			if (viewport_width == 0)
				return 0;
			int n = (x + viewport_width / 2) / viewport_width;
			if (n < 0)
				return 0;
			if (n > max)
				return max;
			return n;
		}

		/**
		 * Converts a screen number to horizontal position within pager
		 * @param n - Screen number
		 * @return Horizontal scroll position
		 */
		private int n2x(int n) {
			return viewport_width * n;
		}

		/**
		 * Smooth scroll to screen unless screen is currently touched
		 * @param num - Number of screen to scroll to
		 */
		public void trySmoothScrollToScreen(int num) {
			if (scrollFrom == -1)
				smoothScrollTo(n2x(num), 0);
		}

		protected void onMeasure(int wms, int hms) {
			viewport_width = MeasureSpec.getSize(wms);
			super.onMeasure(wms, hms);
		}

		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			super.onLayout(changed, l, t, r, b);
			scrollTo(n2x(tablist.getCheckedRadioButtonId()), 0);
		}
	}

	class ScrollContents extends ViewGroup {
		private static final int ID = 42;
		public ScrollContents(Context ctx) {
			super(ctx);
			setFocusableInTouchMode(true); // otherwise children will steal focus
			setId(ID);
		}

		protected void onMeasure(int wms, int hms) {
			wms = MeasureSpec.makeMeasureSpec(viewport_width, MeasureSpec.EXACTLY);
			final int count = getChildCount();
			for (int i = 0; i < count; i++)
				getChildAt(i).measure(wms, hms);
			setMeasuredDimension(viewport_width * count, MeasureSpec.getSize(hms));
		}

		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final View child = getChildAt(i);
				child.layout(l, t, l + viewport_width, b);
				l += viewport_width;
			}
		}
	}
}
