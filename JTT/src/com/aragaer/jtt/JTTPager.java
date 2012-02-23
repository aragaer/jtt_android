package com.aragaer.jtt;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
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
    private final ArrayList<Integer> measures = new ArrayList<Integer>();
    private LayoutParams btnlp;
    private int flags;
    private static final int m = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

    public static final int TABS_BACK = 0;
    public static final int TABS_FRONT = 1;
    public static final int TABS_STRETCH = 0;
    public static final int TABS_WRAP = 2;
    public static final int TAB_TEXT_CENTER = 0;
    public static final int TAB_TEXT_LEFT = 4;

    private int fill_or_wrap;
    private Boolean isVertical;

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

        doSetOrientation(getOrientation());
    }

    @Override
    public void setOrientation(int o) {
        final int oo = getOrientation();
        super.setOrientation(o);
        if (o != oo)
            doSetOrientation(o);
    }

    private void doSetOrientation(int orientation) {
        if (orientation == LinearLayout.HORIZONTAL) {
            isVertical = false;
            tablist.setOrientation(LinearLayout.VERTICAL);
            tablist.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    fill_or_wrap, 0.0f));
            btnlp.width = LayoutParams.WRAP_CONTENT;
            btnlp.height = fill_or_wrap;
        } else {
            isVertical = true;
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
        b.setSingleLine();
        if ((flags & TAB_TEXT_LEFT) > 0)
            b.setGravity(Gravity.LEFT);
        tabs.add(b);
        tablist.addView(b);
        view.setPadding(5, 5, 5, 5);
        pageview.addView(view);

        doMeasure(id);

        if (pageview.mCurrentScreen == -1) {
            pageview.mCurrentScreen = 0;
            select_tab(0);
        } else
            deselect_tab(id);

        return id;
    }

    public void removeTabAt(int id) {
        final View tab = tabs.remove(id);
        if (pageview.mCurrentScreen == id && id > 0)
            pageview.snapToScreen(id - 1);
        tablist.removeView(tab);
        pageview.removeViewAt(id);
        measures.remove(id);
    }

    public void renameTabAt(int pos, String name) {
        tabs.get(pos).setText(name);
        doMeasure(pos);
    }

    protected void deselect_tab(int num) {
        final Button b = tabs.get(num);
        b.setEllipsize(TruncateAt.END);
        b.setSelected(false);
    }

    protected void select_tab(int num) {
        final Button b = tabs.get(num);
        b.setEllipsize(null);
        b.setSelected(true);
        if (isVertical)
            doResizeAround(num);
    }

    private void doMeasure(int pos) {
        final Button b = tabs.get(pos);
        b.measure(m, m);
        Log.d(TAG, "Button "+b.getText()+" will use "+b.getMeasuredWidth()+":"+b.getMeasuredHeight());
        if (measures.size() < pos)
            measures.set(pos, b.getMeasuredWidth());
        else
            measures.add(pos, b.getMeasuredWidth());
    }

    private void doResizeAround(int pos) {
        final int fixed = measures.get(pos);
        final int totalWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int totalWanted = 0;
        Log.d(TAG, "We want to see "+tabs.get(pos).getText()+" having size "+measures.get(pos));
        for (Integer i : measures)
            totalWanted += i;
        Log.d(TAG, "Total available width is "+totalWidth+", we want "+totalWanted+", with "+fixed+" being shown fully");
        float proportion = (float) (totalWidth - fixed) / (totalWanted - fixed);
        if (proportion > 1)
            proportion = (float) totalWidth / totalWanted;
        Log.d(TAG, "Reduce all inactive tabs to "+Math.round(proportion*100)+"%");
        for (int i = 0; i < tabs.size(); i++) {
            final Button b = tabs.get(i);
            int w = measures.get(i);
            if (proportion > 1 || i != pos)
                w *= proportion;
            b.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), m);
            Log.d(TAG, "Button "+b.getText()+" gets shrinked from "+measures.get(i)+" to "+b.getMeasuredWidth()+" (wanted "+w+")");
        }

        int left = 0;
        for (Button b : tabs) {
            final int w = b.getMeasuredWidth();
            b.layout(left, 0, left+w, b.getMeasuredHeight());
            left += w;
        }
        tablist.layout(0, 0, getMeasuredWidth(), tablist.getMeasuredHeight());
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