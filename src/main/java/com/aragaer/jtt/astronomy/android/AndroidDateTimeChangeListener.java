package com.aragaer.jtt.astronomy.android;
// vim: et ts=4 sts=4 sw=4

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.aragaer.jtt.astronomy.DayIntervalService;


public class AndroidDateTimeChangeListener extends BroadcastReceiver {
    private DayIntervalService service;

    public void setService(DayIntervalService service) {
        this.service = service;
    }

    public void register(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        context.registerReceiver(this, filter);
    }

    public void onReceive(Context context, Intent intent) {
        service.timeChanged(System.currentTimeMillis());
    }
}
