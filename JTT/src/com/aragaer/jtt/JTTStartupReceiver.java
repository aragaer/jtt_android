package com.aragaer.jtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class JTTStartupReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean("jtt_bootup", true))
            context.startService(new Intent(context, JTTService.class));
    }
}