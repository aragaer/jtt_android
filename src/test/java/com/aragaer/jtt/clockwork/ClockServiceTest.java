package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import dagger.ObjectGraph;
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

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.aragaer.jtt.Settings;
import com.aragaer.jtt.astronomy.*;
import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.location.TestLocationProvider;
import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;
import static com.aragaer.jtt.clockwork.android.Chime.ACTION_JTT_TICK;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk=18)
public class ClockServiceTest {

    private TestAstrolabe astrolabe;
    private TestChime chime;
    private TestCalculator calculator;
    private ObjectGraph graph;

    @Before public void setUp() {
        TestModule module = new TestModule(new AndroidMetronome(Robolectric.application));
        graph = ObjectGraph.create(module);
        calculator = (TestCalculator) graph.get(DayIntervalCalculator.class);
        chime = (TestChime) graph.get(Chime.class);
    }

    @Test public void shouldConstructAllObjects() {
        Location location = new Location(2, 3);
        TestLocationProvider.setNextResult(location);

        TestChime chime = new TestChime();
        TestMetronome metronome = new TestMetronome();
        Clock clock = new Clock(chime, metronome);
        TestCalculator calculator = new TestCalculator();
        TestAstrolabe astrolabe = new TestAstrolabe(calculator);

        clock.bindToAstrolabe(astrolabe);

        TestLocationProvider locationProvider = new TestLocationProvider();
        locationProvider.setAstrolabe(astrolabe);
        locationProvider.postInit();

        assertThat(astrolabe.currentLocation, equalTo(location));
    }

    @Test public void shouldDingChimesWhenStarted() {
        long tickLength = 1000;
        int tickNumber = 42;
        long inTickOffset = 250;
        long intervalLength = tickLength * TICKS_PER_INTERVAL;
        long startOffset = -(tickNumber * tickLength + inTickOffset);
        long endOffset = startOffset + intervalLength;
        long now = System.currentTimeMillis();

        DayInterval interval = DayInterval.Night(now + startOffset, now + endOffset);
        calculator.setNextResult(interval);

        ServiceController<ClockService> controller = prepareService();
        graph.inject(controller.get());
        controller.startCommand(0, 0);
        checkTickServiceRunning(now + startOffset, tickLength);
        new TickServiceMock().onHandleIntent(null);

        assertThat("chime number", chime.getLastTick(), equalTo(tickNumber));
    }

    @Test public void shouldUseDayTime() {
        long tickLength = 1000;
        int tickNumber = 42;
        long inTickOffset = 250;
        long intervalLength = tickLength * TICKS_PER_INTERVAL;
        long startOffset = -(tickNumber * tickLength + inTickOffset);
        long endOffset = startOffset + intervalLength;
        long now = System.currentTimeMillis();

        calculator.setNextResult(DayInterval.Day(now + startOffset, now + endOffset));

        ServiceController<ClockService> controller = prepareService();
        graph.inject(controller.get());
        controller.startCommand(0, 0);
        new TickServiceMock().onHandleIntent(null);

        assertThat("chime number", chime.getLastTick(), equalTo(tickNumber + TICKS_PER_INTERVAL));
    }

    @Test public void shouldWorkWithoutTestModule() {
        TestReceiver receiver = new TestReceiver();
        Robolectric.application.registerReceiver(receiver, new IntentFilter(ACTION_JTT_TICK));
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

        prepareService().startCommand(0, 0);
        checkTickServiceRunning(interval.getStart(), tickLength);
        new TickServiceMock().onHandleIntent(null);

        int tickNumber = (int) ((now - interval.getStart()) / tickLength);
        if (interval.isDay())
            tickNumber += TICKS_PER_INTERVAL;
        assertThat("chime number", receiver.wrapped, equalTo(tickNumber));
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

    private ServiceController<ClockService> prepareService() {
        ServiceController<ClockService> controller = Robolectric.buildService(ClockService.class);
        controller.attach()
                  .create()
                  .withIntent(new Intent(Robolectric.application, ClockService.class));
        return controller;
    }

    private ServiceController<ClockService> startService() {
        return prepareService().startCommand(0, 0);
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
}
