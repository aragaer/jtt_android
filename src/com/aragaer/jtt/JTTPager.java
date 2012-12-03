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

public class JTTPager extends LinearLayout {
	private PageScroller scrollview;
	private ScrollContents pageview;
	private RadioGroup tablist;
	int viewport_width;
	private RadioGroup.LayoutParams btnlp;

	public JTTPager(Context context) {
		super(context);

		scrollview = new PageScroller(context);
		pageview = new ScrollContents(context);
		scrollview.addView(pageview);

		tablist = new RadioGroup(context);
		tablist.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				scrollToScreen(checkedId);
			}
		});
		setPadding(5, 5, 5, 5);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setOrientation(LinearLayout.VERTICAL);
			tablist.setOrientation(LinearLayout.HORIZONTAL);
			tablist.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT, 0));
			btnlp = new RadioGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.FILL_PARENT, 1);
		} else {
			setOrientation(LinearLayout.HORIZONTAL);
			tablist.setOrientation(LinearLayout.VERTICAL);
			tablist.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.FILL_PARENT, 0));
			btnlp = new RadioGroup.LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT, 1);
		}

		addView(scrollview, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, 1));
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

	protected void select_tab(int num) {
		tablist.check(num);
	}

	public void scrollToScreen(int num) {
		scrollview.scrollToScreen(num);
	}

	public int getScreen() {
		return tablist.getCheckedRadioButtonId();
	}

	class PageScroller extends HorizontalScrollView {
		boolean touch;
		int minfling;

		public PageScroller(Context ctx) {
			super(ctx);
			setHorizontalScrollBarEnabled(false);
			setSmoothScrollingEnabled(true);
			setHorizontalFadingEdgeEnabled(false);
			ViewConfiguration cfg = ViewConfiguration.get(ctx);
			minfling = cfg.getScaledMinimumFlingVelocity();
		}

		VelocityTracker vt = null;
		public boolean onTouchEvent(MotionEvent event) {
			if (vt == null)
				vt = VelocityTracker.obtain();
			vt.addMovement(event);

			if (event.getAction() == MotionEvent.ACTION_UP) {
				vt.computeCurrentVelocity(1000);
				onRelease((int) vt.getXVelocity());
				vt.recycle();
				vt = null;
			} else
				touch = true;
			return super.onTouchEvent(event);
		}

		private int x2n(int x) {
			int max = tablist.getChildCount() - 1;
			int w = getWidth();
			if (w == 0)
				return 0;
			int n = (x + w / 2) / w;
			if (n < 0)
				return 0;
			if (n > max)
				return max;
			return n;
		}

		private int n2x(int n) {
			int w = getWidth();
			return w * n;
		}

		protected void onScrollChanged(int l, int t, int oldl, int oldt) {
			if (touch)
				select_tab(x2n(l));
		}

		public void scrollToScreen(int num) {
			if (!touch)
				smoothScrollTo(n2x(num), 0);
		}

		public void fling(int velocityX) {
			return; // it's already handled
		}

		/* only fling one page */
		public void onRelease(int velocityX) {
			final int x = getScrollX();
			final int bump = getWidth() / 2 + 1;
			int on = x2n(x), nn = on;

			if (Math.abs(velocityX) > minfling)
				nn = x2n(x + (velocityX < 0 ? bump : -bump));

			select_tab(on);
			touch = false;

			if (nn == on)
				smoothScrollTo(n2x(on), 0);
			else
				select_tab(nn); // this will scroll too
		}

		protected void onMeasure(int wms, int hms) {
			viewport_width = MeasureSpec.getSize(wms);
			super.onMeasure(wms, hms);
		}
	}

	class ScrollContents extends ViewGroup {
		public ScrollContents(Context ctx) {
			super(ctx);
			setFocusableInTouchMode(true); // otherwise children will steal focus
			setId(42);
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
