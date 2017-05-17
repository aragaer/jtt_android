// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.resources;

import android.content.Context;


public class RuntimeResources {
    private final Context context;
    private static RuntimeResources instance;
    private static StringResources srInstance;

    private RuntimeResources(final Context ctx) {
        context = ctx;
    }

    public static RuntimeResources get(final Context c) {
        if (instance == null)
            instance = new RuntimeResources(c.getApplicationContext());
        return instance;
    }

    public StringResources getStringResources() {
        if (srInstance == null)
            srInstance = new StringResources(context);
        return srInstance;
    }
}
