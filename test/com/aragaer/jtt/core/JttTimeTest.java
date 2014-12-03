package com.aragaer.jtt.core;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;

import static com.aragaer.jtt.core.JttTime.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;


public class JttTimeTest {

    @Test
    public void ticksToHour() {
        assertThat(JttTime.fromTicks(0).hour, equalTo(JttTime.Hour.COCK));
        assertThat(JttTime.fromTicks(TICKS_PER_HOUR).hour, equalTo(JttTime.Hour.DOG));
        assertThat(JttTime.fromTicks(2*TICKS_PER_HOUR).hour, equalTo(JttTime.Hour.BOAR));
        assertThat(JttTime.fromTicks(3*TICKS_PER_HOUR).hour, equalTo(JttTime.Hour.RAT));
        assertThat(JttTime.fromTicks(4*TICKS_PER_HOUR).hour, equalTo(JttTime.Hour.OX));
        assertThat(JttTime.fromTicks(5*TICKS_PER_HOUR).hour, equalTo(JttTime.Hour.TIGER));
        assertThat(JttTime.fromTicks(6*TICKS_PER_HOUR).hour, equalTo(JttTime.Hour.HARE));
        assertThat(JttTime.fromTicks(7*TICKS_PER_HOUR).hour, equalTo(JttTime.Hour.DRAGON));
        assertThat(JttTime.fromTicks(8*TICKS_PER_HOUR).hour, equalTo(JttTime.Hour.SERPENT));
        assertThat(JttTime.fromTicks(9*TICKS_PER_HOUR).hour, equalTo(JttTime.Hour.HORSE));
        assertThat(JttTime.fromTicks(10*TICKS_PER_HOUR).hour, equalTo(JttTime.Hour.RAM));
        assertThat(JttTime.fromTicks(11*TICKS_PER_HOUR).hour, equalTo(JttTime.Hour.MONKEY));
    }

    @Test
    public void firstQUarterOfCock() {
        JttTime time = JttTime.fromTicks(TICKS_PER_DAY-2*TICKS_PER_QUARTER);
        assertThat(time.hour, equalTo(JttTime.Hour.COCK));
        assertThat(time.quarter, equalTo(JttTime.Quarter.FIRST));
    }

    @Test
    public void thirdQuarterOfCock() {
        JttTime time = JttTime.fromTicks(TICKS_PER_QUARTER);
        assertThat(time.hour, equalTo(JttTime.Hour.COCK));
        assertThat(time.quarter, equalTo(JttTime.Quarter.FOURTH));
    }

    @Test
    public void firstQuarterOfTiger() {
        JttTime time = JttTime.fromTicks(5*TICKS_PER_HOUR - TICKS_PER_QUARTER);
        assertThat(time.hour, equalTo(JttTime.Hour.TIGER));
        assertThat(time.quarter, equalTo(JttTime.Quarter.SECOND));
    }
}
