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
        JttTime time = JttTime.fromTicks(TICKS_PER_DAY - 2*TICKS_PER_QUARTER + 5);
        assertThat(time.hour, equalTo(JttTime.Hour.COCK));
        assertThat(time.quarter, equalTo(JttTime.Quarter.FIRST));
        assertThat(time.ticks, equalTo(5));
    }

    @Test
    public void thirdQuarterOfCock() {
        JttTime time = JttTime.fromTicks(TICKS_PER_QUARTER + 1);
        assertThat(time.hour, equalTo(JttTime.Hour.COCK));
        assertThat(time.quarter, equalTo(JttTime.Quarter.FOURTH));
        assertThat(time.ticks, equalTo(1));
    }

    @Test
    public void firstQuarterOfTiger() {
        JttTime time = JttTime.fromTicks(5*TICKS_PER_HOUR - TICKS_PER_QUARTER);
        assertThat(time.hour, equalTo(JttTime.Hour.TIGER));
        assertThat(time.quarter, equalTo(JttTime.Quarter.SECOND));
        assertThat(time.ticks, equalTo(0));
    }

    @Test
    public void glyphs() {
        assertThat(JttTime.Hour.COCK.glyph, equalTo("酉"));
        assertThat(JttTime.Hour.DOG.glyph, equalTo("戌"));
        assertThat(JttTime.Hour.BOAR.glyph, equalTo("亥"));
        assertThat(JttTime.Hour.RAT.glyph, equalTo("子"));
        assertThat(JttTime.Hour.OX.glyph, equalTo("丑"));
        assertThat(JttTime.Hour.TIGER.glyph, equalTo("寅"));
        assertThat(JttTime.Hour.HARE.glyph, equalTo("卯"));
        assertThat(JttTime.Hour.DRAGON.glyph, equalTo("辰"));
        assertThat(JttTime.Hour.SERPENT.glyph, equalTo("巳"));
        assertThat(JttTime.Hour.HORSE.glyph, equalTo("午"));
        assertThat(JttTime.Hour.RAM.glyph, equalTo("未"));
        assertThat(JttTime.Hour.MONKEY.glyph, equalTo("申"));
    }
}
