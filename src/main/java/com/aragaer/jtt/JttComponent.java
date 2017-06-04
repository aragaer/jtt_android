// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import com.aragaer.jtt.astronomy.AstronomyModule;
import com.aragaer.jtt.astronomy.SolarEventCalculator;
import com.aragaer.jtt.core.*;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules={AstronomyModule.class, CoreModule.class})
public interface JttComponent {
    public Clockwork provideClockwork();
    public IntervalProvider provideIntervalProvider();
    public SolarEventCalculator provideSolarEventCalculator();
}
