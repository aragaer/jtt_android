package com.aragaer.jtt.clockwork.android;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlarmManager.ScheduledAlarm;
import org.robolectric.shadows.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import android.app.AlarmManager;
import android.content.*;

import com.aragaer.jtt.clockwork.TickClient;
import com.aragaer.jtt.clockwork.TickService;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class AndroidMetronomeTest {

    private AndroidMetronome metronome;
    private TestTickClient client;

    @Before
    public void setup() {
        metronome = new AndroidMetronome(Robolectric.application);
        client = new TestTickClient();
        TickService counter = new TickService(metronome);
        counter.addClient(client);
    }

    @Test
    public void shouldScheduleAlarm() {
        metronome.start(0, 100);
        List<ScheduledAlarm> alarms = getScheduledAlarms();
        assertThat(alarms.size(), equalTo(1));
        ScheduledAlarm alarm = alarms.get(0);
        ShadowPendingIntent pending = Robolectric.shadowOf(alarm.operation);
        assertTrue(pending.isServiceIntent());
    }

    @Test
    public void shouldScheduleAlarmWithCorrectStartTime() {
        metronome.start(30, 100);
        List<ScheduledAlarm> alarms = getScheduledAlarms();
        assertThat(alarms.size(), equalTo(1));
        ScheduledAlarm alarm = alarms.get(0);
        assertThat(alarm.triggerAtTime, equalTo(30L));
    }

    @Test
    public void shouldScheduleAlarmWithCorrectInterval() {
        metronome.start(0, 100);
        List<ScheduledAlarm> alarms = getScheduledAlarms();
        assertThat(alarms.size(), equalTo(1));
        ScheduledAlarm alarm = alarms.get(0);
        assertThat(alarm.interval, equalTo(100L));
    }

    @Test
    public void shouldUnscheduleAlarm() {
        metronome.start(0, 100);
        metronome.stop();
        List<ScheduledAlarm> alarms = getScheduledAlarms();
        assertThat(alarms.size(), equalTo(0));
    }

    @Test
    public void shouldTriggerOnTick() throws Exception {
        metronome.start(System.currentTimeMillis()-3, 10);
        Thread.sleep(7);
        assertThat(client.ticks, equalTo(0));
        new MockTicker().onHandleIntent(null);
        assertThat(client.ticks, equalTo(1));
    }

    @Test
    public void shouldMultiTickOnStart() {
        long tickLen = 1000;
        long offset = tickLen * 42 + 750;

        assertThat(client.ticks, equalTo(0));
        metronome.start(System.currentTimeMillis() - offset, tickLen);
        new MockTicker().onHandleIntent(null);
        assertThat(client.ticks, equalTo(42));
    }

    private List<ScheduledAlarm> getScheduledAlarms() {
        AlarmManager am = (AlarmManager) Robolectric.application
            .getSystemService(Context.ALARM_SERVICE);
        return Robolectric.shadowOf(am).getScheduledAlarms();
    }

    class MockTicker extends Ticker {
        @Override
        public void onHandleIntent(Intent intent) {
            super.onHandleIntent(intent);
        }
    }

    private static class TestTickClient implements TickClient {
        public int ticks;
        public void tickChanged(int ticks) {
            this.ticks = ticks;
        }
    }
}
