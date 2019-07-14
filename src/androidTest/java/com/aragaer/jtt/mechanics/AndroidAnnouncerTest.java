// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.mechanics;

import org.junit.*;

import android.content.*;

import com.aragaer.jtt.core.*;

import static org.junit.Assert.*;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;


public class AndroidAnnouncerTest {

    private static final IntentFilter filter = new IntentFilter(AndroidTicker.ACTION_JTT_TICK);
    private Context context;
    private Announcer announcer;
    private TestIntervalProvider intervalProvider;

    @Before public void setUp() {
        context = getInstrumentation().getTargetContext();
        intervalProvider = new TestIntervalProvider();
        announcer = new AndroidAnnouncer(context, intervalProvider);
        context.removeStickyBroadcast(new Intent(AndroidTicker.ACTION_JTT_TICK));
    }

    @After public void tearDown() {
        context.removeStickyBroadcast(new Intent(AndroidTicker.ACTION_JTT_TICK));
    }

    @Test public void testStickyBroadcast() {
        assertNull(context.registerReceiver(null, filter));
        announcer.announce(0);
        Intent sticky = context.registerReceiver(null, filter);
        assertNotNull(sticky);
    }

    @Test public void testSendsData() {
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
