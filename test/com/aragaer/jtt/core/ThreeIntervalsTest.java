// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;


public class ThreeIntervalsTest {

    @Test public void testCreate() {
        ThreeIntervals ti = new ThreeIntervals(new long[] {0, 1, 2, 3});
        assertArrayEquals(ti.getTransitions(), new long[] {0, 1, 2, 3});
    }
}
