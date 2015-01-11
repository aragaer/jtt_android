package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import android.content.Context;

import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.astronomy.SscCalculator;


@Module
public class AndroidClock {
    @Provides @Singleton DayIntervalCalculator getCalculator() {
        return new SscCalculator();
    }
}
