package com.aragaer.jtt;

import android.content.Context;
import android.content.res.Configuration;
import android.view.MotionEvent;
import android.view.View;
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
		int target;
		boolean touched;
		int SNAPPER_DELAY = 200;

		public PageScroller(Context ctx) {
			super(ctx);
			setFillViewport(true);
			setHorizontalScrollBarEnabled(false);
			setSmoothScrollingEnabled(true);
			setHorizontalFadingEdgeEnabled(false);
		}

		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				touched = true;
				break;
			case MotionEvent.ACTION_UP:
				touched = false;
				postDelayed(snapper, SNAPPER_DELAY);
				break;
			default:
				break;
			}
			return super.onTouchEvent(event);
		}

		protected void onScrollChanged(int l, int t, int oldl, int oldt) {
			removeCallbacks(snapper);
			int w = getWidth();
			if (w == 0)
				return;
			select_tab((l + w / 2) / w);
			postDelayed(snapper, SNAPPER_DELAY);
		}

		public void scrollToScreen(int num) {
			removeCallbacks(snapper);
			target = num;
			postDelayed(snapper, SNAPPER_DELAY);
		}

		Runnable snapper = new Runnable() {
			public void run() {
				if (!touched)
					smoothScrollTo(getWidth() * target, 0);
			}
		};

		/* only fling one page */
		public void fling(int velocityX) {
			removeCallbacks(snapper);
			final int bump = getWidth() / 2 + 1;
			smoothScrollBy(velocityX < 0 ? -bump : bump, 0);
			postDelayed(snapper, SNAPPER_DELAY);
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
		}

		protected void onMeasure(int wms, int hms) {
			wms = MeasureSpec.makeMeasureSpec(viewport_width, MeasureSpec.EXACTLY);
			final int count = getChildCount();
			for (int i = 0; i < count; i++)
				getChildAt(i).measure(wms, hms);
			setMeasuredDimension(MeasureSpec.makeMeasureSpec(viewport_width
					* count, MeasureSpec.EXACTLY), hms);
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
