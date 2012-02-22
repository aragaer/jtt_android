package com.aragaer.jtt;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class JTTPager extends LinearLayout {
    private static final String TAG = JTTPager.class.getSimpleName();
    protected JTTPageView pageview;
    private LinearLayout tablist;
    private Context ctx;
    protected final ArrayList<Button> tabs = new ArrayList<Button>();
    private LayoutParams btnlp;

    public static final int TABS_BACK = 0;
    public static final int TABS_FRONT = 1;
    public static final int TABS_STRETCH = 0;
    public static final int TABS_WRAP = 2;

    private int fill_or_wrap;

    public JTTPager(Context context, int flags) {
        this(context, null, flags);
    }

    public JTTPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JTTPager(Context context, AttributeSet attrs, int flags) {
        super(context, attrs);
        ctx = context;
        if ((flags & TABS_WRAP) > 0)
            fill_or_wrap = LayoutParams.WRAP_CONTENT;
        else
            fill_or_wrap = LayoutParams.FILL_PARENT;
        btnlp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.FILL_PARENT, 1.0f);

        pageview = new JTTPageView(ctx);
        pageview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT, 1.0f));

        tablist = new LinearLayout(ctx);
        if ((flags & TABS_FRONT) > 0) {
            addView(tablist);
            addView(pageview);
        } else {
            addView(pageview);
            addView(tablist);
        }

        setOrientation(getOrientation());
    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(orientation);
        if (orientation == LinearLayout.HORIZONTAL) {
            tablist.setOrientation(LinearLayout.VERTICAL);
            tablist.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    fill_or_wrap, 0.0f));
            btnlp.width = LayoutParams.WRAP_CONTENT;
            btnlp.height = fill_or_wrap;
        } else {
            tablist.setOrientation(LinearLayout.HORIZONTAL);
            tablist.setLayoutParams(new LayoutParams(fill_or_wrap,
                    LayoutParams.WRAP_CONTENT, 0.0f));
            btnlp.height = LayoutParams.WRAP_CONTENT;
            btnlp.width = fill_or_wrap;
        }

        for (Button b : tabs)
            b.setLayoutParams(btnlp);
    }

    OnClickListener click = new OnClickListener() {
        public void onClick(View v) {
            final Button btn = (Button) v;
            if (pageview.mCurrentScreen != btn.getId())
                pageview.snapToScreen(btn.getId());
        }
    };

    public int addTab(View view, String btn) {
        final int id = tabs.size();
        final Button b = new Button(ctx, null);
        b.setText(btn);
        b.setOnClickListener(click);
        b.setId(id);
        b.setLayoutParams(btnlp);
        tabs.add(b);
        tablist.addView(b);
        pageview.addView(view);

        if (pageview.mCurrentScreen == -1) {
            pageview.mCurrentScreen = 0;
            select_tab(0);
        }

        return id;
    }

    public void removeTabsStartingFrom(int id) {
        int i = tabs.size();
        if (pageview.mCurrentScreen > id)
            pageview.snapToScreen(id - 1);
        while (--i > id) {
            Log.d(TAG, "removing tab "+tabs.get(i).getText());
            tabs.remove(i);
            tablist.removeViewAt(id + 1);
            pageview.removeViewAt(id + 1);
        }
    }

    public void renameTabAt(int pos, String name) {
        tabs.get(pos).setText(name);
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

    protected final class JTTPageView extends ViewGroup {
        private static final int SNAP_VELOCITY = 1000;

        private boolean mFirstLayout = true;
        private VelocityTracker mVelocityTracker;
        private int mMaximumVelocity;
        private int mTouchSlop;

        protected int mCurrentScreen = -1;

        private float mLastMotionX;
        // private float mLastMotionY;
        private int mScrollX;
        // private int mScrollY;

        private final static int TOUCH_STATE_REST = 0;
        private final static int TOUCH_STATE_SCROLLING = 1;
        private int mTouchState = TOUCH_STATE_REST;

        public JTTPageView(Context ctx) {
            this(ctx, null);
        }
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

            if (width > 0 && mFirstLayout) {
                setHorizontalScrollBarEnabled(false);
                scrollTo(mCurrentScreen * width, 0);
                mFirstLayout = false;
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
            // final float y = ev.getY();
            switch (action) {
            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                // final int yDiff = (int) Math.abs(y - mLastMotionY);

                final int touchSlop = mTouchSlop;
                final boolean xMoved = xDiff > touchSlop;
                // final boolean yMoved = yDiff > touchSlop;

                if (xMoved)
                    mTouchState = TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mLastMotionX = x;
                // mLastMotionY = y;
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
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000,
                            mMaximumVelocity);
                    int velocityX = (int) velocityTracker.getXVelocity();

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
            JTTPager.this.deselect_tab(mCurrentScreen);
            mCurrentScreen = whichScreen;
            JTTPager.this.select_tab(mCurrentScreen);
        }
    }
}