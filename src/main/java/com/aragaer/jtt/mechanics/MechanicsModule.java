// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.mechanics;

import javax.inject.Singleton;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

import com.aragaer.jtt.core.Clockwork;
import com.aragaer.jtt.core.IntervalProvider;


@Module
public class MechanicsModule {

    private final Context _context;

    public MechanicsModule(Context context) {
        _context = context;
    }

    @Singleton @Provides public Ticker provideTicker(Clockwork clockwork,
                                                     Announcer announcer) {
        return new AndroidTicker(clockwork, announcer);
    }

    @Provides public Announcer provideAnnouncer(IntervalProvider provider) {
        return new AndroidAnnouncer(_context, provider);
    }
}
