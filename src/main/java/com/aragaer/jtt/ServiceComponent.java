// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import javax.inject.Singleton;

import dagger.Component;

import com.aragaer.jtt.astronomy.AstronomyModule;
import com.aragaer.jtt.astronomy.SolarEventCalculator;
import com.aragaer.jtt.core.Clockwork;
import com.aragaer.jtt.core.CoreModule;
import com.aragaer.jtt.core.IntervalProvider;
import com.aragaer.jtt.mechanics.Ticker;
import com.aragaer.jtt.mechanics.MechanicsModule;


@Singleton
@Component(modules={AstronomyModule.class,
                    CoreModule.class,
                    MechanicsModule.class})
public interface ServiceComponent {
    public Ticker getTicker();
    public Clockwork provideClockwork();
    public IntervalProvider provideIntervalProvider();
    public SolarEventCalculator provideSolarEventCalculator();
}
