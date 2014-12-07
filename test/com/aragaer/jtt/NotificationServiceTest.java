package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.*;
import org.robolectric.util.ServiceController;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.*;
import android.content.res.Resources;
import android.view.*;
import android.widget.*;

import com.aragaer.jtt.core.JttTime;

import static com.aragaer.jtt.clockwork.Chime.*;
import static org.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk=18, shadows={com.aragaer.jtt.test.ShadowNotificationBuilder.class})
public class NotificationServiceTest {

    private NotificationManager notificationManager;

    @Before
    public void setUp() {
        notificationManager = (NotificationManager) Robolectric.application.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Test
    public void testBroadcastReceiverRegistered() {
        assertNotNull(findListener());
    }

    @Test
    public void shouldStartService() {
        invokeListener(0);
        Intent intent = Robolectric.getShadowApplication().getNextStartedService();
        assertThat("started service", intent.getComponent().getClassName(), equalTo(NotificationService.class.getCanonicalName()));
    }

    @Test
    public void shouldDisplayNotification() {
        assertNotNull(simulateNotification(0));
    }

    @Test
    public void shouldBuildCorrectNotification() {
        Notification notification = simulateNotification(0);

        RemoteViews content = notification.contentView;
        assertThat("Is ongoing",
                   notification.flags & Notification.FLAG_ONGOING_EVENT,
                   is(not(0)));
        assertThat("Only alert once",
                   notification.flags & Notification.FLAG_ONLY_ALERT_ONCE,
                   is(not(0)));
        assertThat("Is foreground",
                   notification.flags,
                   is(0));
        assertThat("Content layout id", content.getLayoutId(), equalTo(R.layout.notification));

        View notificationView = LayoutInflater.from(Robolectric.application).inflate(R.layout.notification, null);
        content.reapply(Robolectric.application, notificationView);
        JttTime time = JttTime.fromTicks(0);

        assertThat(getTextView(notificationView, R.id.image), equalTo(time.hour.glyph));
        assertThat(getTextView(notificationView, R.id.title), equalTo(longHourName(time.hour)));
        assertThat(getTextView(notificationView, R.id.quarter), equalTo(quarterName(time.quarter)));

        // TODO: Finish this!
    }

    private static String getTextView(View parent, int viewId) {
        return ((TextView) parent.findViewById(viewId)).getText().toString();
    }

    private String longHourName(JttTime.Hour hour) {
        return Robolectric.application.getResources().getStringArray(R.array.hour_of)[hour.ordinal()];
    }

    private String quarterName(JttTime.Quarter quarter) {
        return Robolectric.application.getResources().getStringArray(R.array.quarter)[quarter.ordinal()];
    }

    private Notification simulateNotification(int ticks) {
        invokeListener(ticks);
        NotificationService service = startServiceFromIntent(Robolectric.getShadowApplication().getNextStartedService());
        return shadowOf(service).getLastForegroundNotification();
    }

    private NotificationService startServiceFromIntent(Intent intent) {
        ServiceController<NotificationService> controller = Robolectric.buildService(NotificationService.class);
        controller.attach().create().withIntent(intent).startCommand(0, 0);
        return controller.get();
    }

    private void invokeListener(int ticks) {
        findListener().onReceive(Robolectric.application, new Intent(ACTION_JTT_TICK).putExtra(EXTRA_JTT, ticks));
    }

    private NotificationService.JttTimeListener findListener() {
        List<ShadowApplication.Wrapper> registeredReceivers = Robolectric.getShadowApplication().getRegisteredReceivers();

        assertFalse(registeredReceivers.isEmpty());

        String name = NotificationService.JttTimeListener.class.getSimpleName();

        for (ShadowApplication.Wrapper wrapper : registeredReceivers)
            if (name.equals(wrapper.broadcastReceiver.getClass().getSimpleName()))
                return (NotificationService.JttTimeListener) wrapper.broadcastReceiver;

        return null;
    }
}
