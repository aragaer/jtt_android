package com.aragaer.jtt;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Scroller;

public class JTTPager extends LinearLayout {
	private JTTPageView pageview;
	private RadioGroup tablist;
	private LayoutParams lp;
	private RadioGroup.LayoutParams btnlp = new RadioGroup.LayoutParams(
			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1);

	public JTTPager(Context context, AttributeSet attrs) {
		super(context, attrs);

		lp = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);

		pageview = new JTTPageView(context, attrs);
		pageview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, 1.0f));

		tablist = new RadioGroup(context, attrs);
		tablist.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				pageview.snapToScreen(checkedId);
			}
		});
		addView(pageview);
		addView(tablist);
		setPadding(5, 5, 5, 5);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setOrientation(LinearLayout.VERTICAL);
			tablist.setOrientation(LinearLayout.HORIZONTAL);
			tablist.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT, 0));
		} else {
			setOrientation(LinearLayout.HORIZONTAL);
			tablist.setOrientation(LinearLayout.VERTICAL);
			tablist.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.FILL_PARENT, 0));
		}
	}

	public int addTab(View view, int resid) {
		final int id = tablist.getChildCount();
		final RadioButton b = new RadioButton(getContext(), null);
		b.setText(getContext().getString(resid));
		b.setId(id);
		tablist.addView(b, btnlp);
		pageview.addView(view, lp);

		if (id == 0)
			select_tab(0);

		return id;
	}

	protected void select_tab(int num) {
		tablist.check(num);
	}

	public void scrollToScreen(int num) {
		pageview.snapToScreen(num);
	}

	public int getScreen() {
		return tablist.getCheckedRadioButtonId();
	}

	private final class JTTPageView extends ViewGroup {
		Scroller scroller = new Scroller(getContext());
		private static final int SNAP_VELOCITY = 1000;

		private VelocityTracker vt;
		private int mMaximumVelocity;
		private int mTouchSlop;

		int mCurrentScreen = -1;

		private float last_x;
		private int scroll_x;

		private final static int TOUCH_STATE_REST = 0;
		private final static int TOUCH_STATE_SCROLLING = 1;
		private int touch_state = TOUCH_STATE_REST;

		public JTTPageView(Context ctx, AttributeSet attrs) {
			super(ctx, attrs);

			setHapticFeedbackEnabled(false);

			final ViewConfiguration cfg = ViewConfiguration.get(getContext());
			mMaximumVelocity = cfg.getScaledMaximumFlingVelocity();
			mTouchSlop = cfg.getScaledTouchSlop();

			setHorizontalScrollBarEnabled(false);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			// The children are given the same width and height as the workspace
			final int count = getChildCount();
			for (int i = 0; i < count; i++)
				getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right,
				int bottom) {
			int childLeft = 0;
			final int width = right - left;

			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final View child = getChildAt(i);
				child.layout(childLeft, 0, childLeft + width,
						child.getMeasuredHeight());
				childLeft += width;
			}
			if (width > 0)
				scrollTo(mCurrentScreen * width, 0);
		}

		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			final int action = ev.getAction();
			if (action == MotionEvent.ACTION_MOVE
					&& touch_state != TOUCH_STATE_REST)
				return true;

			final float x = ev.getX();
			switch (action) {
			case MotionEvent.ACTION_MOVE:
				if (Math.abs(x - last_x) > mTouchSlop)
					touch_state = TOUCH_STATE_SCROLLING;
				break;

			case MotionEvent.ACTION_DOWN:
				// Remember location of down touch
				last_x = x;
				touch_state = TOUCH_STATE_REST;
				break;

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				// Release the drag
				touch_state = TOUCH_STATE_REST;
				break;
			}

			/*
			 * The only time we want to intercept motion events is if we are in
			 * the drag mode.
			 */
			return touch_state != TOUCH_STATE_REST;
		}

		@Override
		public boolean onTouchEvent(MotionEvent ev) {
			if (vt == null)
				vt = VelocityTracker.obtain();
			vt.addMovement(ev);

			final int action = ev.getAction();
			final float x = ev.getX();

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				// Remember where the motion event started
				last_x = x;
				break;
			case MotionEvent.ACTION_MOVE:
				int deltaX = (int) (last_x - x);
				touch_state = TOUCH_STATE_SCROLLING;
				// Scroll to follow the motion event
				last_x = x;

				final int maxWidth = (getChildCount() - 1) * getWidth();

				if (scroll_x + deltaX < 0)
					deltaX = -scroll_x;
				else if (scroll_x + deltaX > maxWidth)
					deltaX = maxWidth - scroll_x;

				scrollBy(deltaX, 0);
				break;
			case MotionEvent.ACTION_UP:
				if (touch_state == TOUCH_STATE_SCROLLING) {
					final int bump = getWidth() / 2 + 1;
					vt.computeCurrentVelocity(1000, mMaximumVelocity);
					int velocityX = (int) vt.getXVelocity();

					if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0)
						// Fling hard enough to move left
						scrollBy(-bump, 0);
					else if (velocityX < -SNAP_VELOCITY
							&& mCurrentScreen < getChildCount() - 1)
						// Fling hard enough to move right
						scrollBy(bump, 0);

					snapToScreen(mCurrentScreen);

					vt.recycle();
					vt = null;
				}
				touch_state = TOUCH_STATE_REST;
				break;
			case MotionEvent.ACTION_CANCEL:
				touch_state = TOUCH_STATE_REST;
			}

			return true;
		}

		@Override
		public void scrollBy(int scrollX, int scrollY) {
			scroll_x += scrollX;
			super.scrollBy(scrollX, scrollY);
			selectDestination();
		}

		@Override
		public void scrollTo(int scrollX, int scrollY) {
			scroll_x = scrollX;
			super.scrollTo(scrollX, scrollY);
			selectDestination();
		}

		private void selectDestination() {
			final int w = getWidth();
			if (w == 0)
				return;
			final int whichScreen = (int) (scroll_x + w / 2) / w;

			selectScreen(whichScreen);
		}

		public void snapToScreen(int whichScreen) {
			final int x = getWidth() * whichScreen;
			selectScreen(whichScreen);
			super.scrollTo(x, 0);
			scroll_x = x;
		}

		public void selectScreen(int whichScreen) {
			mCurrentScreen = whichScreen;
			select_tab(mCurrentScreen);
		}
	}
}