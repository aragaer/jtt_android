package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import android.content.*;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.TestDayIntervalService;
import com.aragaer.jtt.JttService;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class DateTimeChangeListenerTest {

    private DateTimeChangeListener listener;
    private TestDayIntervalService astrolabe;

    @Before
    public void setup() {
        TestModule module = new TestModule();
        Clock clock = new Clock(module.getChime(), module.getMetronome());
        astrolabe = new TestDayIntervalService();
        clock.bindToDayIntervalService(astrolabe);
        astrolabe.setNextResult(DayInterval.Day(0, 0));
        listener = new DateTimeChangeListener(astrolabe);
        listener.register(Robolectric.application);
    }

    private void testListensFor(String action) {
        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        Intent intent = new Intent(action);
        List<BroadcastReceiver> receiversForIntent = shadowApplication.getReceiversForIntent(intent);
        assertThat(receiversForIntent.size(), equalTo(1));
    }

    @Test
    public void shouldListenForTimeChange() {
        testListensFor(Intent.ACTION_TIME_CHANGED);
    }

    @Test
    public void shouldListenForDateChange() {
        testListensFor(Intent.ACTION_DATE_CHANGED);
    }

    @Test public void testShouldReportToDayIntervalService() {
        int oldCount = this.astrolabe.dateTimeChangeCalls;
        listener.onReceive(Robolectric.application, new Intent(Intent.ACTION_DATE_CHANGED));
        int newCount = this.astrolabe.dateTimeChangeCalls;

        assertThat(newCount, equalTo(oldCount+1));
    }
}
