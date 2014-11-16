package com.aragaer.jtt.test;
// vim: et ts=4 sts=4 sw=4

import android.app.Notification;

import org.robolectric.annotation.*;
import org.robolectric.shadows.*;


@Implements(Notification.Builder.class)
public class ShadowNotificationBuilder extends ShadowNotification.ShadowBuilder {
    @Implementation
    public Notification.Builder setContentTitle(CharSequence title) {
        if (title == null)
            title = "";
        return super.setContentTitle(title);
    }

    @Implementation
    public Notification.Builder setContentText(CharSequence title) {
        if (title == null)
            title = "";
        return super.setContentText(title);
    }
}
