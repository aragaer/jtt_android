// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.resources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.content.Context;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class RuntimeResourcesTest {

    @Mock Context mockContext;

    @Before public void setUp() {
	when(mockContext.getApplicationContext()).thenReturn(mockContext);
    }

    @Test public void test_canGetForContext() {
	RuntimeResources rr = RuntimeResources.get(mockContext);
	assertNotNull(rr);
    }

    @Test public void test_sameContextReturnsSameResources() {
	RuntimeResources rr = RuntimeResources.get(mockContext);
	RuntimeResources rr2 = RuntimeResources.get(mockContext);

	assertEquals(rr, rr2);
    }
}
