package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.*;
import org.robolectric.shadows.ShadowAlarmManager.ScheduledAlarm;
import org.robolectric.util.ServiceController;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import android.app.*;
import android.content.*;

import static com.aragaer.jtt.TickBroadcast.ACTION_JTT_TICK;
import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;
import com.aragaer.jtt.location.*;
import com.aragaer.jtt.astronomy.*;
import com.aragaer.jtt.clockwork.android.TickService;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk=18)
public class JttServiceTest {

    @Test public void shouldUseAndroidMetronome() {
        ServiceController<JttService> controller = startService();

        List<ScheduledAlarm> alarms = getScheduledAlarms();
        assertThat(alarms.size(), equalTo(1));

		ScheduledAlarm alarm = alarms.get(0);
		ShadowPendingIntent pending = Robolectric.shadowOf(alarm.operation);
        ShadowIntent intent = Robolectric.shadowOf(pending.getSavedIntent());

		assertTrue(pending.isServiceIntent());
        assertEquals(pending.getSavedContext(), controller.get());
        assertEquals(intent.getIntentClass(), TickService.class);
    }

    @Test public void shouldUseTickBroadcast() {
        TestReceiver receiver = new TestReceiver();
        Robolectric.application.registerReceiver(receiver, new IntentFilter(ACTION_JTT_TICK));
        ServiceController<JttService> controller = startService();

        assertThat(receiver.calls, equalTo(0));

        new TickServiceMock().onHandleIntent(null);

        assertThat(receiver.calls, equalTo(1));
    }

    @Test public void shouldUseAndroidLocationProviderAndSscCalculator() {
        SharedPreferences sharedPreferences = ShadowPreferenceManager.getDefaultSharedPreferences(Robolectric.application.getApplicationContext());
        sharedPreferences.edit().putString(Settings.PREF_LOCATION, "1.2:3.4").commit();
        Location location = new Location(1.2, 3.4);
        DayIntervalCalculator realCalculator = new SscCalculator();
        realCalculator.setLocation(location);
        long now = System.currentTimeMillis();
        DayInterval interval = realCalculator.getIntervalFor(now);

        long tickLength = interval.getLength() / TICKS_PER_INTERVAL;
        assertThat(interval.getStart(), lessThan(now));
        assertThat(interval.getEnd(), greaterThan(now));
        // FIXME: it is possible that interval has ended already at this point - the test will fail
        // Have to verify that we stay in the same interval for the duration of the test

        startService();

        checkTickServiceRunning(interval.getStart(), tickLength);
    }

    @Test public void shouldUseAndroidLocationChangeNotifier() {
        SharedPreferences sharedPreferences = ShadowPreferenceManager.getDefaultSharedPreferences(Robolectric.application.getApplicationContext());
        sharedPreferences.edit().putString(Settings.PREF_LOCATION, "1.2:3.4").commit();
        Location location = new Location(5.6, 7.8);
        DayIntervalCalculator realCalculator = new SscCalculator();
        realCalculator.setLocation(location);
        long now = System.currentTimeMillis();
        DayInterval interval = realCalculator.getIntervalFor(now);

        long tickLength = interval.getLength() / TICKS_PER_INTERVAL;
        assertThat(interval.getStart(), lessThan(now));
        assertThat(interval.getEnd(), greaterThan(now));
        // FIXME: it is possible that interval has ended already at this point - the test will fail
        // Have to verify that we stay in the same interval for the duration of the test
        startService();

        sharedPreferences.edit().putString(Settings.PREF_LOCATION, "5.6:7.8").commit();

        checkTickServiceRunning(interval.getStart(), tickLength);
    }

    private void checkTickServiceRunning(long start, long period) {
        List<ScheduledAlarm> alarms = getScheduledAlarms();
        assertThat(alarms.size(), equalTo(1));

        ScheduledAlarm alarm = alarms.get(0);
        assertThat("tick service start time", alarm.triggerAtTime, equalTo(start));
        assertThat("tick service period", alarm.interval, equalTo(period));

        ShadowPendingIntent pending = Robolectric.shadowOf(alarm.operation);
        ShadowIntent intent = Robolectric.shadowOf(pending.getSavedIntent());

        assertTrue(pending.isServiceIntent());
        assertEquals(intent.getIntentClass(), TickService.class);
    }

    static class TestReceiver extends BroadcastReceiver {
        int wrapped = -1;
        int calls;
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(ACTION_JTT_TICK))
                return;
            calls++;
            wrapped = intent.getIntExtra("jtt", 0);
        }
    }

    private ServiceController<JttService> startService() {
        ServiceController<JttService> controller = Robolectric.buildService(JttService.class);
        controller.attach()
                  .create()
                  .withIntent(new Intent(Robolectric.application, JttService.class))
                  .startCommand(0, 0);
        return controller;
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
