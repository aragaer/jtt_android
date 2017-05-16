// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.resources;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;


public class RuntimeResources {
    protected final Context context;
    private final static Map<Context, RuntimeResources> rr_singletons = new HashMap<Context, RuntimeResources>();
    private final static Map<Class<?>, Object> singletons = new HashMap<Class<?>, Object>();

    private RuntimeResources(final Context ctx) {
        context = ctx;
        rr_singletons.put(context, this);
    }

    public static RuntimeResources get(final Context c) {
        final Context app_c = c.getApplicationContext();
        return rr_singletons.containsKey(app_c)
            ? rr_singletons.get(app_c)
            : new RuntimeResources(app_c);
    }

    @SuppressWarnings("unchecked")
    public final <E> E getInstance(final Class<E> clazz) {
        Constructor<E> ctor;
        try {
            ctor = clazz.getDeclaredConstructor(Context.class);
        } catch (NoSuchMethodException e) {
            Log.e("RR", "Failed to find constructor", e);
            return null;
        }
        E res = (E) singletons.get(clazz);
        if (res == null)
            try {
                res = ctor.newInstance(context);
                singletons.put(clazz, res);
            } catch (IllegalArgumentException e) {
                Log.e("RR", "Failed to call constructor", e);
            } catch (InstantiationException e) {
                Log.e("RR", "Failed to call constructor", e);
            } catch (IllegalAccessException e) {
                Log.e("RR", "Failed to call constructor", e);
            } catch (InvocationTargetException e) {
                Log.e("RR", "Failed to call constructor", e);
            }
        return res;
    }
}
