// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import com.aragaer.jtt.astronomy.SolarEventCalculator;

import dagger.Module;
import dagger.Provides;


@Module
public class CoreModule {

    @Provides public IntervalProvider provideIntervalProvider(SolarEventCalculator calculator) {
	return new SscCalculator(calculator);
    }

    @Provides public Clockwork provideClockwork(IntervalProvider intervalProvider) {
	return new Clockwork(intervalProvider);
    }
}
