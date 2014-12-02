package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import java.util.List;
import java.util.TimeZone;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ServiceController;
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
import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.location.LocationProvider;
import com.aragaer.jtt.core.JttTime;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk=18)
public class ClockServiceTest {

    private TestLocationProvider locationProvider;
    private TestCalculator calculator;
    private TestChime chime;

    @Before
    public void setUp() {
        locationProvider = new TestLocationProvider();
        calculator = new TestCalculator();
        chime = new TestChime();
        ClockService.setLocationProvider(locationProvider);
        ClockService.setDayIntervalCalculator(calculator);
        ClockService.setChime(chime);
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

        calculator.setNextResult(DayInterval.Night(now + startOffset, now + endOffset));

        ServiceController<ClockService> controller = startService();
        checkTickServiceRunning(controller.get(), now + startOffset, tickLength);
        new TickServiceMock().onHandleIntent(null);

        assertThat("chime number", chime.getLastTick(), equalTo(tickNumber));
    }

    @Test
    public void shouldUseDayTime() {
        long tickLength = 1000;
        int tickNumber = 42;
        long inTickOffset = 250;
        long intervalLength = tickLength * JttTime.TICKS_PER_INTERVAL;
        long startOffset = -(tickNumber * tickLength + inTickOffset);
        long endOffset = startOffset + intervalLength;
        long now = System.currentTimeMillis();

        calculator.setNextResult(DayInterval.Day(now + startOffset, now + endOffset));

        startService();
        new TickServiceMock().onHandleIntent(null);

        assertThat("chime number", chime.getLastTick(), equalTo(tickNumber + JttTime.TICKS_PER_INTERVAL));
    }

    private void checkTickServiceRunning(Context context, long start, long period) {
        List<ScheduledAlarm> alarms = getScheduledAlarms();
        assertThat(alarms.size(), equalTo(1));

        ScheduledAlarm alarm = alarms.get(0);
        assertThat("tick service start time", alarm.triggerAtTime, equalTo(start));
        assertThat("tick service period", alarm.interval, equalTo(period));

        ShadowPendingIntent pending = Robolectric.shadowOf(alarm.operation);
        ShadowIntent intent = Robolectric.shadowOf(pending.getSavedIntent());

        assertTrue(pending.isServiceIntent());
        assertEquals(pending.getSavedContext(), context);
        assertEquals(intent.getIntentClass(), TickService.class);
    }

    @Test
    public void shouldPassLocationFromProviderToCalculatorWhenStarted() {
        Location location = new Location(1, 2);
        locationProvider.setNextResult(location);
        calculator.setNextResult(DayInterval.Night(0, 1));

        startService();

        assertThat("location passed to calculator", calculator.location, equalTo(location));
    }

    private ServiceController<ClockService> startService() {
        ServiceController<ClockService> controller = Robolectric.buildService(ClockService.class);
        controller.attach()
                  .create()
                  .withIntent(new Intent(Robolectric.application, ClockService.class))
                  .startCommand(0, 0);
        return controller;
    }

    static class TestLocationProvider implements LocationProvider {

        private Location nextResult;

        public void setNextResult(Location location) {
            nextResult = location;
        }

        public Location getCurrentLocation() {
            return nextResult;
        }
    }

    private static class TestCalculator implements DayIntervalCalculator {

        private DayInterval nextResult;
        public Location location;

        public void setNextResult(DayInterval interval) {
            nextResult = interval;
        }

        public DayInterval getIntervalFor(long timestamp) {
            return nextResult;
        }

        public void setLocation(Location newLocation) {
            location = newLocation;
        }
    }

    public static class TestChime extends Chime {
        private int ticks;

        public TestChime() {
            super(null);
        }

        public int getLastTick() {
            return ticks;
        }

        @Override
        public void ding(int ticks) {
            this.ticks = ticks;
        }
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
