package com.aragaer.jtt;

import java.util.ArrayList;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;

public class ViewPagerAdapter extends PagerAdapter implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener {

	ArrayList<View> views = new ArrayList<View>();
	private final ViewPager pager;
	private RadioGroup tablist;
	private final ActionBarActivity context;

	public ViewPagerAdapter(final ActionBarActivity ctx, final ViewPager pager) {
		this.pager = pager;
		context = ctx;
		tablist = new RadioGroup(ctx);
		tablist.setOnCheckedChangeListener(this);
		tablist.setOrientation(LinearLayout.HORIZONTAL);
		ctx.getSupportActionBar().setCustomView(tablist, new ActionBar.LayoutParams(
		        ViewGroup.LayoutParams.MATCH_PARENT,
		        ViewGroup.LayoutParams.MATCH_PARENT));
		pager.setOnPageChangeListener(this);
	}

	@Override
	public int getCount() {
		return views.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	public void addView(View view, int resid) {
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
	public Object instantiateItem(ViewGroup container, int position) {
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
