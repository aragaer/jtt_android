// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.mechanics;

import org.junit.*;

import android.os.*;

import com.aragaer.jtt.core.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;


public class AndroidTickerTest {

    private TestClockwork clockwork;
    private AndroidTicker ticker;
    private TestAnnouncer announcer;

    @BeforeClass public static void setUpClass() {
        Looper.prepare();
    }

    @AfterClass public static void tearDownClass() {
        Looper looper = Looper.myLooper();
        if (looper != null)
            looper.quit();
    }

    @Before public void setUp() {
        clockwork = new TestClockwork();
        announcer = new TestAnnouncer();
        ticker = new AndroidTicker(clockwork, announcer);
    }

    @Test public void testStart() {
        assertFalse("No messages yet", ticker.hasMessages(0));
        ticker.start();
        assertTrue("Should have a message queued", ticker.hasMessages(0));
    }

    @Test public void testStop() {
        ticker.start();
        ticker.stop();
        assertFalse("Should have no messages", ticker.hasMessages(0));
    }

    @Test public void testHandleMessage() {
        assertFalse("Should have no messages", ticker.hasMessages(0));
        long before = System.currentTimeMillis();
        ticker.handleMessage(null);
        long after = System.currentTimeMillis();
        assertThat("Clockwork requested not earlier than start",
                   clockwork.stamp, greaterThanOrEqualTo(before));
        assertThat("Clockwork requested not later than stop",
                   clockwork.stamp, lessThanOrEqualTo(after));
        assertThat("Event is not earlier than we started",
                   announcer.stamp, greaterThanOrEqualTo(before));
        assertThat("Event is not later than we stopped",
                   announcer.stamp, lessThanOrEqualTo(after));
    }

    // TODO: Extract the next tick calculation to Clockwork
    @Ignore("Next tick calculation should be extracted to Clockwork")
    @Test public void testQueueNext() {
        TestTicker ticker = new TestTicker(clockwork, announcer);
        long realBefore = SystemClock.uptimeMillis();
        long timestampToElapsed = System.currentTimeMillis() - realBefore;
        long realAfter = SystemClock.uptimeMillis();
        long fuzzy = realAfter - realBefore;
        long before = System.currentTimeMillis();
        ticker.handleMessage(null);
        long after = System.currentTimeMillis();
        Looper.loop();
        assertNotNull("Should actually have a real queued message",
                      ticker.handled);
        assertThat("The message was queued not too early", ticker.when + fuzzy,
                   greaterThanOrEqualTo(roundToNextTick(before) - timestampToElapsed));
        assertThat("The message was queued not too late", ticker.when - fuzzy,
                   lessThanOrEqualTo(roundToNextTick(after) - timestampToElapsed));
    }

    private long roundToNextTick(long stamp) {
        stamp -= clockwork.start;
        stamp -= stamp % clockwork.repeat;
        stamp += clockwork.start + clockwork.repeat;
        return stamp;
    }
}

class TestClockwork extends Clockwork {
    long stamp;

    TestClockwork() {
        super(null);
        start = 0;
        repeat = 1000;
    }

    @Override public void setTime(long timestamp) {
        stamp = timestamp;
    }
}

class TestTicker extends AndroidTicker {

    Message handled;
    long when;

    TestTicker(Clockwork clockwork, Announcer announcer) {
        super(clockwork, announcer);
    }

    @Override public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg != null) {
            removeMessages(0);
            handled = msg;
            Looper looper = Looper.myLooper();
            assertNotNull(looper);
            looper.quitSafely();
            assertNotNull(handled);
            when = handled.getWhen();
        }
    }
}

class TestAnnouncer implements Announcer {
    long stamp;

    @Override public void announce(long timestamp) {
        stamp = timestamp;
        synchronized (this) {
            notify();
        }
    }
}
