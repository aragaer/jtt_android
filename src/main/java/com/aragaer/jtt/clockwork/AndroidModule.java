package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import dagger.*;
import javax.inject.Singleton;

import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.astronomy.SscCalculator;

import android.content.Context;


@Module(injects={Astrolabe.class, Chime.class, Clock.class,
    DateTimeChangeListener.class, com.aragaer.jtt.clockwork.android.Chime.class})
public class AndroidModule {

    private final Context context;

    public AndroidModule(Context context) {
        this.context = context;
    }

    @Provides @Singleton Context getContext() {
        return context;
    }

    @Provides @Singleton Chime getChime() {
        com.aragaer.jtt.clockwork.android.Chime result = new com.aragaer.jtt.clockwork.android.Chime();
        ObjectGraph.create(this).inject(result);
        return result;
    }

    @Provides @Singleton Metronome getMetronome() {
        return new AndroidMetronome(context);
    }

    @Provides @Singleton DayIntervalCalculator getCalculator() {
        return new SscCalculator();
    }
}
