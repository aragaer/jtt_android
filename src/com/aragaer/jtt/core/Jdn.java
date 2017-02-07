// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;


public class Jdn {
    private static final double MS_PER_DAY = 60*60*24*1000;
    private static final double UNIX_EPOCH_JD = 2440587.5;

    public static long fromTimestamp(long unix_ms) {
        return (long) Math.floor(unix_ms/MS_PER_DAY + UNIX_EPOCH_JD);
    }

    public static long toTimestamp(long jdn) {
        return Math.round((jdn - UNIX_EPOCH_JD) * MS_PER_DAY);
    }
}
