package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import javax.inject.Inject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class DateTimeChangeListener extends BroadcastReceiver {
    private final Astrolabe astrolabe;

    @Inject public DateTimeChangeListener(Astrolabe astrolabe) {
        this.astrolabe = astrolabe;
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
            astrolabe.onDateTimeChanged();
    }
}
