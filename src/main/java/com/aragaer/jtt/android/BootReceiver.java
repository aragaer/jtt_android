// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.android;

import com.aragaer.jtt.JttService;

import android.content.*;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(new Intent(context, JttService.class));
            else
                context.startService(new Intent(context, JttService.class));
        }
    }
}
