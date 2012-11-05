package com.aragaer.jtt;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class JTTPager extends LinearLayout {
	private JTTPageView pageview;
	private LinearLayout tablist;
	protected final ArrayList<Button> tabs = new ArrayList<Button>();
	private LayoutParams btnlp, lp;

	public JTTPager(Context context, AttributeSet attrs) {
		super(context, attrs);

		lp = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);

		pageview = new JTTPageView(context, attrs);
		pageview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, 1.0f));

		tablist = new LinearLayout(context, attrs);
		addView(pageview);
		addView(tablist);
		setPadding(5, 5, 5, 5);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setOrientation(LinearLayout.VERTICAL);
			tablist.setOrientation(LinearLayout.HORIZONTAL);
			tablist.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT, 0.0f));
			btnlp = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.FILL_PARENT, 1.0f);
		} else {
			setOrientation(LinearLayout.HORIZONTAL);
			tablist.setOrientation(LinearLayout.VERTICAL);
			tablist.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.FILL_PARENT, 0.0f));
			btnlp = new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT, 1.0f);
		}

		for (Button b : tabs)
			b.setLayoutParams(btnlp);
	}

	OnClickListener click = new OnClickListener() {
		public void onClick(View v) {
			final int btn_id = v.getId();
			if (pageview.mCurrentScreen != btn_id)
				pageview.snapToScreen(btn_id);
		}
	};

	public int addTab(View view, int resid) {
		return addTab(view, getContext().getString(resid));
	}

	public int addTab(View view, String btn) {
		final int id = tabs.size();
		final Button b = new Button(getContext(), null);
		b.setText(btn);
		b.setOnClickListener(click);
		b.setId(id);
		b.setLayoutParams(btnlp);
		tabs.add(b);
		tablist.addView(b);
		pageview.addView(view, lp);

		if (pageview.mCurrentScreen == -1) {
			pageview.mCurrentScreen = 0;
			select_tab(0);
		}

		return id;
	}

	protected void deselect_tab(int num) {
		tabs.get(num).setSelected(false);
	}

	protected void select_tab(int num) {
		tabs.get(num).setSelected(true);
	}

	public void scrollToScreen(int num) {
		pageview.snapToScreen(num);
	}

	public int getScreen() {
		return pageview.mCurrentScreen;
	}

	private final class JTTPageView extends ViewGroup {
		private static final int SNAP_VELOCITY = 1000;

		private VelocityTracker mVelocityTracker;
		private int mMaximumVelocity;
		private int mTouchSlop;

		protected int mCurrentScreen = -1;

		private float mLastMotionX;
		private int mScrollX;

		private final static int TOUCH_STATE_REST = 0;
		private final static int TOUCH_STATE_SCROLLING = 1;
		private int mTouchState = TOUCH_STATE_REST;

		public JTTPageView(Context ctx, AttributeSet attrs) {
			super(ctx, attrs);

			setHapticFeedbackEnabled(false);

			final ViewConfiguration cfg = ViewConfiguration.get(getContext());
			mMaximumVelocity = cfg.getScaledMaximumFlingVelocity();
			mTouchSlop = cfg.getScaledTouchSlop();
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			final int width = MeasureSpec.getSize(widthMeasureSpec);

			// The children are given the same width and height as the workspace
			final int count = getChildCount();
			for (int i = 0; i < count; i++)
				getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);

			if (width > 0) {
				setHorizontalScrollBarEnabled(false);
				scrollTo(mCurrentScreen * width, 0);
			}
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right,
				int bottom) {
			int childLeft = 0;

			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final View child = getChildAt(i);
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth,
						child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}

		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			final int action = ev.getAction();
			if (action == MotionEvent.ACTION_MOVE
					&& mTouchState != TOUCH_STATE_REST)
				return true;

			final float x = ev.getX();
			switch (action) {
			case MotionEvent.ACTION_MOVE:
				if (Math.abs(x - mLastMotionX) > mTouchSlop)
					mTouchState = TOUCH_STATE_SCROLLING;
				break;

			case MotionEvent.ACTION_DOWN:
				// Remember location of down touch
				mLastMotionX = x;
				mTouchState = TOUCH_STATE_REST;
				break;

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				// Release the drag
				mTouchState = TOUCH_STATE_REST;
				break;
			}

			/*
			 * The only time we want to intercept motion events is if we are in
			 * the drag mode.
			 */
			return mTouchState != TOUCH_STATE_REST;
		}

		@Override
		public boolean onTouchEvent(MotionEvent ev) {
			if (mVelocityTracker == null)
				mVelocityTracker = VelocityTracker.obtain();
			mVelocityTracker.addMovement(ev);

			final int action = ev.getAction();
			final float x = ev.getX();

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				// Remember where the motion event started
				mLastMotionX = x;
				break;
			case MotionEvent.ACTION_MOVE:
				int deltaX = (int) (mLastMotionX - x);
				mTouchState = TOUCH_STATE_SCROLLING;
				// Scroll to follow the motion event
				mLastMotionX = x;

				final int maxWidth = (getChildCount() - 1) * getWidth();

				if (mScrollX + deltaX < 0)
					deltaX = -mScrollX;
				else if (mScrollX + deltaX > maxWidth)
					deltaX = maxWidth - mScrollX;

				scrollBy(deltaX, 0);
				break;
			case MotionEvent.ACTION_UP:
				if (mTouchState == TOUCH_STATE_SCROLLING) {
					final int bump = getWidth() / 2 + 1;
					mVelocityTracker.computeCurrentVelocity(1000,
							mMaximumVelocity);
					int velocityX = (int) mVelocityTracker.getXVelocity();

					if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0)
						// Fling hard enough to move left
						scrollBy(-bump, 0);
					else if (velocityX < -SNAP_VELOCITY
							&& mCurrentScreen < getChildCount() - 1)
						// Fling hard enough to move right
						scrollBy(bump, 0);

					snapToScreen(mCurrentScreen);

					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
				mTouchState = TOUCH_STATE_REST;
				break;
			case MotionEvent.ACTION_CANCEL:
				mTouchState = TOUCH_STATE_REST;
			}

			return true;
		}

		@Override
		public void scrollBy(int scrollX, int scrollY) {
			mScrollX += scrollX;
			super.scrollBy(scrollX, scrollY);
			selectDestination();
		}

		@Override
		public void scrollTo(int scrollX, int scrollY) {
			mScrollX = scrollX;
			super.scrollTo(scrollX, scrollY);
			selectDestination();
		}

		private void selectDestination() {
			final int w = getWidth();
			if (w == 0)
				return;
			final int whichScreen = (int) (mScrollX + w / 2) / w;

			selectScreen(whichScreen);
		}

		public void snapToScreen(int whichScreen) {
			final int x = getWidth() * whichScreen;
			selectScreen(whichScreen);
			super.scrollTo(x, 0);
			mScrollX = x;
		}

		public void selectScreen(int whichScreen) {
			deselect_tab(mCurrentScreen);
			mCurrentScreen = whichScreen;
			select_tab(mCurrentScreen);
		}
	}
}