package com.aragaer.jtt.clockwork;
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


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class AndroidMetronomeTest {

    private AndroidMetronome metronome;

    @Before
    public void setup() {
        metronome = new AndroidMetronome(Robolectric.application);
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
        TestClock clock = new TestClock();
        metronome.attachTo(clock);
        metronome.start(System.currentTimeMillis()-3, 10);
        Thread.sleep(7);
        assertThat(clock.ticks, equalTo(0));
        new TickServiceMock().onHandleIntent(null);
        assertThat(clock.ticks, equalTo(1));
    }

    @Test
    public void shouldMultiTickOnStart() {
        long tickLen = 1000;
        long offset = tickLen * 42 + 750;

        TestClock clock = new TestClock();
        metronome.attachTo(clock);
        assertThat(clock.ticks, equalTo(0));
        metronome.start(System.currentTimeMillis() - offset, tickLen);
        new TickServiceMock().onHandleIntent(null);
        assertThat(clock.ticks, equalTo(42));
    }

    private List<ScheduledAlarm> getScheduledAlarms() {
        AlarmManager am = (AlarmManager) Robolectric.application
            .getSystemService(Context.ALARM_SERVICE);
        return Robolectric.shadowOf(am).getScheduledAlarms();
    }

    class TickServiceMock extends TickService {
        @Override
        public void onHandleIntent(Intent intent) {
            super.onHandleIntent(intent);
        }
    }
}
