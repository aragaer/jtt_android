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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.astronomy.TestCalculator;
import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.location.TestLocationProvider;
import com.aragaer.jtt.core.JttTime;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk=18)
public class ClockServiceTest {

    private TestAstrolabe astrolabe;
    private TestChime chime;
    private TestCalculator calculator;
    private ObjectGraph graph;

    @Before
    public void setUp() throws Exception {
        TestClockFactory module = new TestClockFactory(new AndroidMetronome(Robolectric.application));
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

        TestLocationProvider locationProvider = new TestLocationProvider(astrolabe);
        locationProvider.postInit();

        assertThat(astrolabe.currentLocation, equalTo(location));
    }

    @Test
    public void shouldDingChimesWhenStarted() {
        long tickLength = 1000;
        int tickNumber = 42;
        long inTickOffset = 250;
        long intervalLength = tickLength * JttTime.TICKS_PER_INTERVAL;
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

    @Test
    @Ignore
    public void shouldUseDayTime() {
        long tickLength = 1000;
        int tickNumber = 42;
        long inTickOffset = 250;
        long intervalLength = tickLength * JttTime.TICKS_PER_INTERVAL;
        long startOffset = -(tickNumber * tickLength + inTickOffset);
        long endOffset = startOffset + intervalLength;
        long now = System.currentTimeMillis();

        astrolabe.setNextResult(DayInterval.Day(now + startOffset, now + endOffset));

        startService();
        new TickServiceMock().onHandleIntent(null);

        assertThat("chime number", chime.getLastTick(), equalTo(tickNumber + JttTime.TICKS_PER_INTERVAL));
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

    @Test
    @Ignore
    public void shouldPassLocationFromProviderToCalculatorWhenStarted() {
        astrolabe.setNextResult(DayInterval.Night(0, 1));

        startService();

        assertThat("astrolabe updateLocation called", astrolabe.updateLocationCalls, equalTo(1));
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
}
