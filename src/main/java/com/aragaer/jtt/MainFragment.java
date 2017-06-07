// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.*;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.ListView;

import com.aragaer.jtt.core.ThreeIntervals;
import com.aragaer.jtt.mechanics.AndroidTicker;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;
import com.aragaer.jtt.today.TodayAdapter;


public class MainFragment extends Fragment {
    private ClockView clock;
    private TodayAdapter today;
    private ViewPager pager;
    private int tickNumber;
    private int page;
    private ThreeIntervals intervals;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                if (!intent.getAction().equals(AndroidTicker.ACTION_JTT_TICK))
                    return;
                tickNumber = intent.getIntExtra("jtt", 0);

                clock.setHour(tickNumber);
                intervals = (ThreeIntervals) intent.getSerializableExtra("intervals");
                if (intervals == null) {
                    Log.w("JTT", "Got null intervals object");
                    return;
                }
                today.tick(intervals);
            }
        };

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        StringResources.setLocaleToContext(getActivity());
        pager = new ViewPager(getActivity());
        final ViewPagerAdapter pager_adapter = new ViewPagerAdapter(getActivity(), pager);
        clock = new ClockView(getActivity());
        if (savedInstanceState != null) {
            tickNumber = savedInstanceState.getInt("tickNumber", 0);
            page = savedInstanceState.getInt("page", 0);
        }
        clock.setHour(tickNumber);

        final ListView today_list = new ListView(getActivity());
        today = new TodayAdapter(getActivity(), 0, RuntimeResources.get(getActivity()).getStringResources());
        today_list.setAdapter(today);
        today_list.setDividerHeight(-getResources().getDimensionPixelSize(R.dimen.today_divider_neg));
        if (intervals != null)
            today.tick(intervals);

        pager_adapter.addView(clock, R.string.clock);
        pager_adapter.addView(today_list, R.string.today);

        pager.setAdapter(pager_adapter);
        pager.setCurrentItem(page, false);
        getActivity().registerReceiver(receiver, new IntentFilter(AndroidTicker.ACTION_JTT_TICK));
        return pager;
    }

    @Override public void onStart() {
        getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        super.onStart();
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tickNumber", tickNumber);
        outState.putInt("page", page);
    }

    @Override public void onDestroyView() {
        getActivity().unregisterReceiver(receiver);
        page = pager.getCurrentItem();
        super.onDestroyView();
    }
}
