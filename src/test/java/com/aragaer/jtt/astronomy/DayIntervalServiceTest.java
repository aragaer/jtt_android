package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.test.*;


public class DayIntervalServiceTest {

    private DayIntervalService service;
    private TestCalculator2 calculator;
    private TestIntervalClient client;

    @Before public void setup() {
        client = new TestIntervalClient();
        calculator = new TestCalculator2();
        service = new DayIntervalService(calculator);
        service.registerClient(client);
    }

    @Test public void shouldNotifyClientOnLocationChange() {
        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);
        Location location = new Location(4, 5);
        long before = System.currentTimeMillis();

        service.onLocationChanged(location);

        long after = System.currentTimeMillis();
        assertThat(calculator.location, equalTo(location));
        assertThat(client.currentInterval, equalTo(interval));
        assertThat(calculator.timestamps.get(0), greaterThanOrEqualTo(before));
        assertThat(calculator.timestamps.get(0), lessThanOrEqualTo(after));
    }

    @Test public void shouldCalculateNewIntervalForCurrentTime() {
        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);

        service.timeChanged(15000);

        assertThat(calculator.timestamps.get(0), equalTo(15000L));
        assertThat(client.currentInterval, equalTo(interval));
    }

    @Test public void shouldCompareIfNewDateInSameInterval() {
        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);

        int changes = client.intervalChanges;
        service.timeChanged(15000);
        int calculations = calculator.intervalCalls;
        service.timeChanged(17000);

        assertThat(calculator.intervalCalls, equalTo(calculations));
        assertThat(client.intervalChanges, equalTo(changes+1));
    }

    @Test public void multipleClients() {
        TestIntervalClient client2 = new TestIntervalClient();
        service.registerClient(client2);

        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);
        service.timeChanged(15000);

        assertThat(client.currentInterval, equalTo(interval));
        assertThat(client2.currentInterval, equalTo(interval));
    }

    @Test public void currentInterval() {
        DayInterval night = DayInterval.Night(100000, 200000);
        calculator.setNextResult(night);
        service.timeChanged(150000);

        assertThat(service.getCurrentInterval(), equalTo(night));
    }

    @Test public void threeIntervals() {
        DayInterval day1 = DayInterval.Day(0, 300000);
        DayInterval night = DayInterval.Night(300000, 600000);
        DayInterval day2 = DayInterval.Day(600000, 900000);
        calculator.results.put(240000L, day1);
        calculator.results.put(380000L, night);
        calculator.results.put(660000L, day2);
        calculator.setNextResult(null);

        service.timeChanged(380000);

        assertThat(service.getCurrentInterval(), equalTo(night));
        assertThat(service.getPreviousInterval(), equalTo(day1));
        assertThat(service.getNextInterval(), equalTo(day2));
    }

    private static class TestIntervalClient implements DayIntervalClient {
        public DayInterval currentInterval;
        public int intervalChanges;

        public void intervalChanged(DayInterval interval) {
            currentInterval = interval;
            intervalChanges++;
        }
    }

    private static class TestCalculator2 extends TestCalculator {
        public List<Long> timestamps = new LinkedList<Long>();
        public Map<Long, DayInterval> results = new HashMap<Long, DayInterval>();

        public DayInterval getIntervalFor(long timestamp) {
            timestamps.add(timestamp);
            DayInterval result = super.getIntervalFor(timestamp);
            return result == null ? results.get(timestamp) : result;
        }
    }
}
