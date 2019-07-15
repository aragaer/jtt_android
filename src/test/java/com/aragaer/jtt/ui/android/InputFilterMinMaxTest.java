// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.ui.android;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import android.text.Spanned;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class InputFilterMinMaxTest {

    private InputFilterMinMax filter;

    @Before public void setUp() {
        filter = new InputFilterMinMax(-100, 100);
    }

    @Test public void shouldAcceptDeletion() {
        FakeSpanned dest = new FakeSpanned("");
        assertNull(filter.filter("", 0, 0, dest, 0, 0));
        assertNull(filter.filter("word", 0, 0, dest, 0, 0));
        assertNull(filter.filter("word", 1, 1, dest, 0, 0));
    }

    @Test public void shouldAcceptSmallNumbers() {
        assertNull(filter.filter("1", 0, 1, new FakeSpanned(""), 0, 0));
        assertNull(filter.filter("1", 0, 1, new FakeSpanned("0"), 0, 0));
        assertNull(filter.filter("1", 0, 1, new FakeSpanned("0"), 0, 1));
        assertNull(filter.filter("1", 0, 1, new FakeSpanned("0"), 1, 1));
        assertNull(filter.filter("1", 0, 1, new FakeSpanned("22"), 0, 1));
        assertNull(filter.filter("1", 0, 1, new FakeSpanned("22"), 1, 2));
        assertNull(filter.filter("1", 0, 1, new FakeSpanned("222"), 0, 2));
        assertNull(filter.filter("1", 0, 1, new FakeSpanned("222"), 1, 3));
        assertNull(filter.filter(".", 0, 1, new FakeSpanned("1"), 0, 0));
        assertNull(filter.filter(".", 0, 1, new FakeSpanned("01"), 1, 1));
    }

    @Test public void shouldAcceptSingleMinus() {
        assertNull(filter.filter("-", 0, 1, new FakeSpanned(""), 0, 0));
    }

    @Test public void shouldRejectLargeNumbers() {
        assertEquals(filter.filter("200", 0, 3, new FakeSpanned("50"), 0, 2), "50");
        assertEquals(filter.filter("200", 0, 3, new FakeSpanned(""), 0, 0), "");
        assertEquals(filter.filter("20", 0, 2, new FakeSpanned("0"), 0, 0), "");
        assertEquals(filter.filter("22", 0, 2, new FakeSpanned("3"), 1, 1), "");
        assertEquals(filter.filter("1234", 1, 4, new FakeSpanned("78"), 0, 2), "78");
    }

    @Test public void shouldRejectNonNumbers() {
        assertEquals(filter.filter("x", 0, 1, new FakeSpanned(""), 0, 0), "");
        assertEquals(filter.filter("x", 0, 1, new FakeSpanned("10"), 0, 2), "10");
        assertEquals(filter.filter(".", 0, 1, new FakeSpanned("0.1"), 0, 0), "");
        assertEquals(filter.filter("-", 0, 1, new FakeSpanned("12"), 1, 1), "");
        assertEquals(filter.filter("1", 0, 1, new FakeSpanned("-12"), 0, 0), "");
    }

    private static class FakeSpanned implements Spanned {
        private final CharSequence data;

        FakeSpanned(CharSequence data) {
            this.data = data;
        }

        @Override public int getSpanStart(Object tag) {
            return 0;
        }
        @Override public int getSpanEnd(Object tag) {
            return 0;
        }
        @Override public int getSpanFlags(Object tag) {
            return 0;
        }
        @Override public <T> T[] getSpans(int start, int end, Class<T> type) {
            return (T[]) new Object[0];
        }
        @Override public int nextSpanTransition(int start, int end, Class type) {
            return 0;
        }
        @Override public char charAt(int index) {
            return data.charAt(index);
        }
        @Override public int length() {
            return data.length();
        }
        @Override public @NotNull CharSequence subSequence(int start, int end) {
            return data.subSequence(start, end);
        }
        @Override public @NotNull String toString() {
            return data.toString();
        }
    }
}
