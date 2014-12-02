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

import com.aragaer.jtt.JttService;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class TimeDateChangeListenerTest {

    private TimeDateChangeListener listener;
    private ClockProbe clock;

    @Before
    public void setup() {
        clock = new ClockProbe(null, null, new TestMetronome());
        listener = new TimeDateChangeListener(clock);
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

    @Test
    public void testShouldAdjustClock() {
        int oldCount = this.clock.adjustCount;
        listener.onReceive(Robolectric.application, new Intent(Intent.ACTION_DATE_CHANGED));
        int newCount = this.clock.adjustCount;

        assertThat(newCount, equalTo(oldCount+1));
    }

    private static class ClockProbe extends Clock {
        public int adjustCount;

        public ClockProbe(Astrolabe astrolabe, Chime chime, Metronome metronome) {
            super(astrolabe, chime, metronome);
        }

        @Override
        public void adjust() {
            adjustCount++;
        }
    }
}
