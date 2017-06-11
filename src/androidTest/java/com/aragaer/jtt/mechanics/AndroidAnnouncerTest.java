// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.mechanics;

import org.junit.*;
import org.junit.runner.RunWith;

import android.content.*;
import android.support.test.runner.AndroidJUnit4;

import com.aragaer.jtt.core.*;

import static org.junit.Assert.*;

import static android.support.test.InstrumentationRegistry.getTargetContext;


@RunWith(AndroidJUnit4.class)
public class AndroidAnnouncerTest {

    private static IntentFilter filter = new IntentFilter(AndroidTicker.ACTION_JTT_TICK);
    private Context context;
    private Announcer announcer;
    private TestIntervalProvider intervalProvider;

    @Before public void setUp() {
        context = getTargetContext();
        intervalProvider = new TestIntervalProvider();
        announcer = new AndroidAnnouncer(context, intervalProvider);
        context.removeStickyBroadcast(new Intent(AndroidTicker.ACTION_JTT_TICK));
    }

    @After public void tearDown() {
        context.removeStickyBroadcast(new Intent(AndroidTicker.ACTION_JTT_TICK));
    }

    @Test public void testStickyBroadcast() throws Exception {
        assertNull(context.registerReceiver(null, filter));
        announcer.announce(0);
        Intent sticky = context.registerReceiver(null, filter);
        assertNotNull(sticky);
    }

    @Test public void testSendsData() throws Exception {
        long timestamp = 0;
        announcer.announce(timestamp);
        Intent sticky = context.registerReceiver(null, filter);
        assertNotNull(sticky);
        ThreeIntervals intervals = (ThreeIntervals) sticky.getSerializableExtra("intervals");
        assertEquals("Should announce the same data interval provider calculates",
                     intervalProvider.getIntervalsForTimestamp(timestamp), intervals);
        Hour expected = Hour.fromInterval(intervals.getMiddleInterval(), timestamp);
        assertEquals("Should announce the same hour as calculated from interval",
                     expected.wrapped, sticky.getIntExtra("jtt", 0));
        assertEquals("Should announce hour number",
                     expected.num, sticky.getIntExtra("hour", 0));
    }
}

class TestIntervalProvider implements IntervalProvider {
    @Override public ThreeIntervals getIntervalsForTimestamp(long time) {
        int ticklen = 2;
        int intervalLen = Hour.TICKS_PER_INTERVAL * ticklen;
        return new ThreeIntervals(new long[] {time - intervalLen*3/2, time - intervalLen/2,
                                              time + intervalLen/2, time + intervalLen*3/2}, true);
    }
}
