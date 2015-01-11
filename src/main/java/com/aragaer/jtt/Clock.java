package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import javax.inject.Singleton;

import dagger.Component;

import com.aragaer.jtt.astronomy.DayIntervalService;


@Component(modules=AndroidClock.class)
@Singleton
public interface Clock {
    DayIntervalService getDayIntervalService();
}
