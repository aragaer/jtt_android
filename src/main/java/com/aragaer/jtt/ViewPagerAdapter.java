package com.aragaer.jtt;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;

import org.jetbrains.annotations.NotNull;

/* package private */ class ViewPagerAdapter extends PagerAdapter implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener {

	private final ArrayList<View> views = new ArrayList<>();
	private final ViewPager pager;
	private final RadioGroup tablist;
	private final Activity context;

	/* package private */ ViewPagerAdapter(final Activity ctx, final ViewPager pager) {
		this.pager = pager;
		context = ctx;
		tablist = new RadioGroup(ctx);
		tablist.setOnCheckedChangeListener(this);
		tablist.setOrientation(LinearLayout.HORIZONTAL);
		ActionBar actionBar = ctx.getActionBar();
		if (actionBar != null)
			actionBar.setCustomView(tablist, new ActionBar.LayoutParams(
		        ViewGroup.LayoutParams.MATCH_PARENT,
		        ViewGroup.LayoutParams.MATCH_PARENT));
		pager.setOnPageChangeListener(this);
	}

	@Override
	public int getCount() {
		return views.size();
	}

	@Override
	public boolean isViewFromObject(@NotNull View view, @NotNull Object object) {
		return view == object;
	}

	/* package private */ void addView(View view, int resid) {
		final int id = tablist.getChildCount();
		final RadioButton b = new RadioButton(context, null);
		final LayoutParams btnlp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT, 1);
		b.setText(context.getString(resid));
		b.setId(id);
		views.add(view);
		tablist.addView(b, btnlp);
		if (id == 0)
			tablist.check(0);
	}

	@Override
	public @NotNull Object instantiateItem(@NotNull ViewGroup container, int position) {
		container.addView(views.get(position));
		return views.get(position);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		pager.setCurrentItem(checkedId, true);
	}

	@Override
	public void onPageScrollStateChanged(int state) { }

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

	@Override
	public void onPageSelected(int position) {
		tablist.check(position);
	}
}
