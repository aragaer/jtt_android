package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, ClockService.class));
    }
}
