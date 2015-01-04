package com.aragaer.jtt.astronomy.android;
// vim: et ts=4 sts=4 sw=4

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.aragaer.jtt.astronomy.DateTimeChangeListener;
import com.aragaer.jtt.astronomy.DayIntervalService;


public class AndroidDateTimeChangeListener extends BroadcastReceiver
        implements DateTimeChangeListener {
    private DayIntervalService service;


    public void setService(DayIntervalService service) {
        this.service = service;
    }

    public void register(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        context.registerReceiver(this, filter);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIME_CHANGED)
                || action.equals(Intent.ACTION_DATE_CHANGED))
            service.onDateTimeChanged();
    }
}
