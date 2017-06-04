// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class AstronomyModule {

    @Provides @Singleton public SolarEventCalculator provideSolarEventCalculator() {
	return new SscAdapter();
    }
}
