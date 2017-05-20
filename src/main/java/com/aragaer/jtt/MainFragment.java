// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import android.app.Fragment;
import android.content.*;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.ListView;

import com.aragaer.jtt.android.AndroidTicker;
import com.aragaer.jtt.core.ThreeIntervals;
import com.aragaer.jtt.today.TodayAdapter;


public class MainFragment extends Fragment {
    private ClockView clock;
    private TodayAdapter today;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                if (!intent.getAction().equals(AndroidTicker.ACTION_JTT_TICK))
                    return;
                final int wrapped = intent.getIntExtra("jtt", 0);

                clock.setHour(wrapped);
                ThreeIntervals intervals = (ThreeIntervals) intent.getSerializableExtra("intervals");
                if (intervals == null) {
                    Log.w("JTT", "Got null intervals object");
                    return;
                }
                today.tick(intervals);
            }
        };


    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewPager pager = new ViewPager(getContext());
        final ViewPagerAdapter pager_adapter = new ViewPagerAdapter(getActivity(), pager);

        clock = new ClockView(getContext());

        final ListView today_list = new ListView(getContext());
        today = new TodayAdapter(getContext(), 0);
        today_list.setAdapter(today);
        today_list.setDividerHeight(-getResources().getDimensionPixelSize(R.dimen.today_divider_neg));

        pager_adapter.addView(clock, R.string.clock);
        pager_adapter.addView(today_list, R.string.today);

        pager.setAdapter(pager_adapter);
        getContext().registerReceiver(receiver, new IntentFilter(AndroidTicker.ACTION_JTT_TICK));
        return pager;
    }

    @Override public void onDestroy() {
        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }
}
