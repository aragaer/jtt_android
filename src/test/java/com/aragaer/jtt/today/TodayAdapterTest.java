// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.today;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.aragaer.jtt.core.ThreeIntervals;
import com.aragaer.jtt.resources.StringResources;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;


@RunWith(PowerMockRunner.class)
public class TodayAdapterTest {

    private static Context mockContext = mock(Context.class);
    private static StringResources mockSR = mock(StringResources.class);

    private FakeTodayAdapter adapter;
    private ArrayList<TodayItem> initial;

    @Before public void setUp() {
        adapter = new FakeTodayAdapter();

        initial = new ArrayList<TodayItem>();
        initial.add(new HourItem(System.currentTimeMillis()-2000, 0));
        adapter.items.addAll(initial);
    }

    @Test public void testTickWithValidIntervals() {
        long now = System.currentTimeMillis();
        ThreeIntervals intervals = new ThreeIntervals(new long[]{now-360, now-120, now+120, now+360}, true);

        adapter.tick(intervals);

        assertEquals("initial list cleared", 37, adapter.getCount());
        long stamp = now-360;
        HourItem firstHour = (HourItem) adapter.getItem(0);
        assertEquals(stamp, firstHour.time);
        assertEquals(0, firstHour.hnum);
        for (int i = 0; i < 18; i++) {
            BoundaryItem boundary = (BoundaryItem) adapter.getItem(i*2+1);
            HourItem hour = (HourItem) adapter.getItem(i*2+2);
            assertEquals(stamp+20, boundary.time);
            assertEquals(stamp+40, hour.time);
            assertEquals((i+1)%12, hour.hnum);
            stamp+=40;
        }

        assertTrue(adapter.datasetChanged);
    }

    @Test public void testTickWithValidIntervalsAtNight() {
        long now = System.currentTimeMillis();
        ThreeIntervals intervals = new ThreeIntervals(new long[]{now-360, now-120, now+120, now+360}, false);

        adapter.tick(intervals);

        assertEquals("initial list cleared", 37, adapter.getCount());
        long stamp = now-360;
        HourItem firstHour = (HourItem) adapter.getItem(0);
        assertEquals(stamp, firstHour.time);
        assertEquals(6, firstHour.hnum);
        for (int i = 0; i < 18; i++) {
            BoundaryItem boundary = (BoundaryItem) adapter.getItem(i*2+1);
            HourItem hour = (HourItem) adapter.getItem(i*2+2);
            assertEquals(stamp+20, boundary.time);
            assertEquals(stamp+40, hour.time);
            assertEquals((i+7)%12, hour.hnum);
            stamp+=40;
        }

        assertTrue(adapter.datasetChanged);
    }

    @Test public void testTickWithStaleIntervals() {
        long now = System.currentTimeMillis();
        ThreeIntervals intervals = new ThreeIntervals(new long[]{now-1000, now-800, now-600, now-400}, true);

        adapter.tick(intervals);

        assertEquals("should not modify initial list", initial, adapter.items);
        assertFalse(adapter.datasetChanged);
    }

    @Test public void testNotRebuildIfIntervalsIsTheSame() {
        long now = System.currentTimeMillis();
        ThreeIntervals intervals = new ThreeIntervals(new long[]{now-360, now-120, now+120, now+360}, false);
        adapter.tick(intervals);
        ArrayList<TodayItem> saved = new ArrayList<TodayItem>(adapter.items);
        adapter.datasetChanged = false;
        adapter.clearCalled = false;

        adapter.tick(intervals);

        assertFalse("should not clear the list", adapter.clearCalled);
        assertEquals("Still has the same items", saved, adapter.items);
        assertTrue("should update selected item", adapter.datasetChanged);
    }

    @Test public void testNotRebuildIfInRebuild() {
        long now = System.currentTimeMillis();
        ThreeIntervals intervals = new ThreeIntervals(new long[]{now-360, now-120, now+120, now+360}, false);
        adapter.tick(intervals);
        adapter.datasetChanged = false;
        adapter.clearCalled = false;
        adapter.items.clear();

        adapter.tick(intervals);

        assertFalse("should not clear the list", adapter.clearCalled);
        assertEquals("should not add more items to the list", 0, adapter.getCount());
        assertFalse("should not update selected item", adapter.datasetChanged);
    }

    @Test public void testStringChanges() {
        verify(mockSR)
            .registerStringResourceChangeListener(adapter,
                                                  StringResources.TYPE_HOUR_NAME | StringResources.TYPE_TIME_FORMAT);

        adapter.onStringResourcesChanged(0);

        assertTrue("should rebuild list", adapter.datasetChanged);
    }

    @Test public void testInitialize() {
        assertEquals(2, adapter.getViewTypeCount());
        for (int i = 0; i < 37; i++) {
            assertFalse(adapter.isEnabled(i));
            assertEquals(i % 2, adapter.getItemViewType(i));
        }
    }

    @Test public void testViews() {
        long now = System.currentTimeMillis();
        ThreeIntervals intervals = new ThreeIntervals(new long[]{now-360, now-120, now+120, now+360}, false);
        adapter.tick(intervals);

        int selected = 18; // item in position 18 should be selected

        View mockView = mock(View.class);
        ViewGroup mockVG = mock(ViewGroup.class);
        when(mockVG.getContext()).thenReturn(mockContext);
        for (int i = 0; i < 37; i++) {
            TodayItem spyTI = spy(adapter.items.get(i));
            doReturn(mockView).when(spyTI).toView(any(Context.class), any(View.class), anyInt());
            adapter.items.set(i, spyTI);
            adapter.getView(i, mockView, mockVG);
            verify(spyTI).toView(mockContext, mockView, selected - i);
        }
    }

    private static class FakeTodayAdapter extends TodayAdapter {
        public ArrayList<TodayItem> items = new ArrayList<TodayItem>();
        boolean datasetChanged;
        boolean clearCalled;

        FakeTodayAdapter() {
            super(mockContext, 0, mockSR);
        }

        @Override public void clear() {
            items.clear();
            clearCalled = true;
        }

        @Override public void add(TodayItem item) {
            items.add(item);
        }

        @Override public int getCount() {
            return items.size();
        }

        @Override public TodayItem getItem(int position) {
            return items.get(position);
        }

        @Override public void notifyDataSetChanged() {
            datasetChanged = true;
        }
    }
}
