// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class AstronomyModule {

    private final Context _context;

    public AstronomyModule(Context context) {
        _context = context;
    }

    @Provides public SolarEventCalculator provideSolarEventCalculator(LocationHandler locationHandler) {
        return new SscAdapter(locationHandler);
    }

    @Provides public LocationHandler provideLocationHandler() {
        return new AndroidLocationHandler(_context);
    }
}
