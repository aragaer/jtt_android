package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.test.*;


public class DayIntervalServiceTest {

    private DayIntervalService service;
    private TestCalculator calculator;
    private TestIntervalClient client;

    @Before public void setup() {
        client = new TestIntervalClient();
        calculator = new TestCalculator();
        service = new DayIntervalService(calculator);
        service.registerClient(client);
    }

    @Test public void shouldNotifyClientOnDateTimeChange() {
        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);
        long before = System.currentTimeMillis();

        service.onDateTimeChanged();

        long after = System.currentTimeMillis();
        assertThat(client.currentInterval, equalTo(interval));
        assertThat(calculator.timestamp, greaterThanOrEqualTo(before));
        assertThat(calculator.timestamp, lessThanOrEqualTo(after));
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
        assertThat(calculator.timestamp, greaterThanOrEqualTo(before));
        assertThat(calculator.timestamp, lessThanOrEqualTo(after));
    }

    @Test public void shouldCalculateNewIntervalOnIntervalEnd() {
        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);
        long before = System.currentTimeMillis();

        service.onIntervalEnded();

        long after = System.currentTimeMillis();
        assertThat(client.currentInterval, equalTo(interval));
        assertThat(calculator.timestamp, greaterThanOrEqualTo(before));
        assertThat(calculator.timestamp, lessThanOrEqualTo(after));
    }

    private static class TestIntervalClient implements DayIntervalClient {
        public DayInterval currentInterval;

        public void intervalChanged(DayInterval interval) {
            currentInterval = interval;
        }
    }

    private static class TestChangeNotifier implements DateTimeChangeListener {
        public void setService(DayIntervalService service) {}
    }
}
