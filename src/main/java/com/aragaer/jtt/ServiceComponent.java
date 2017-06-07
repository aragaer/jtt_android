// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import javax.inject.Singleton;

import dagger.Component;

import com.aragaer.jtt.mechanics.Ticker;
import com.aragaer.jtt.mechanics.MechanicsModule;


@Singleton
@Component(modules=MechanicsModule.class)
public interface ServiceComponent {
    public Ticker getTicker();
}
