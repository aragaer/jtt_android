package com.aragaer.jtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class JTTStartupReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, JTTService.class));
    }
}