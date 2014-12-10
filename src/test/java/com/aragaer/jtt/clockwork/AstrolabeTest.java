package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import dagger.ObjectGraph;
import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.astronomy.TestCalculator;
import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.test.*;


public class AstrolabeTest {

    private Astrolabe astrolabe;
    private TestCalculator calculator;
    private TestClock clock;

    @Before
    public void setup() {
        ObjectGraph graph = ObjectGraph.create(new TestModule());
        clock = graph.get(TestClock.class);
        astrolabe = graph.get(Astrolabe.class);
        clock.bindToAstrolabe(astrolabe);
        calculator = (TestCalculator) graph.get(DayIntervalCalculator.class);
    }

    // TODO: Remove
    @Test
    public void shouldReturnCalculatorResult() {
        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);

        assertThat(astrolabe.getCurrentInterval(), equalTo(interval));
    }

    @Test public void shouldNotifyClockOnDateTimeChange() {
        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);
        long before = System.currentTimeMillis();

        astrolabe.onDateTimeChanged();

        long after = System.currentTimeMillis();
        assertThat(clock.currentInterval, equalTo(interval));
        assertThat(calculator.timestamp, greaterThanOrEqualTo(before));
        assertThat(calculator.timestamp, lessThanOrEqualTo(after));
    }

    @Test public void shouldNotifyClockOnLocationChange() {
        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);
        Location location = new Location(4, 5);
        long before = System.currentTimeMillis();

        astrolabe.onLocationChanged(location);

        long after = System.currentTimeMillis();
        assertThat(calculator.location, equalTo(location));
        assertThat(clock.currentInterval, equalTo(interval));
        assertThat(calculator.timestamp, greaterThanOrEqualTo(before));
        assertThat(calculator.timestamp, lessThanOrEqualTo(after));
    }

    @Test public void shouldCalculateNewIntervalOnIntervalEnd() {
        DayInterval interval = DayInterval.Day(10000, 20000);
        calculator.setNextResult(interval);
        long before = System.currentTimeMillis();

        astrolabe.onIntervalEnded();

        long after = System.currentTimeMillis();
        assertThat(clock.currentInterval, equalTo(interval));
        assertThat(calculator.timestamp, greaterThanOrEqualTo(before));
        assertThat(calculator.timestamp, lessThanOrEqualTo(after));
    }
}
