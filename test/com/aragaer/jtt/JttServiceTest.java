package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.*;
import org.robolectric.shadows.ShadowAlarmManager.ScheduledAlarm;
import org.robolectric.util.ServiceController;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import android.app.*;
import android.content.*;

import com.aragaer.jtt.core.TransitionProvider;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class JttServiceTest {

	private TransitionProvider transitionProvider;

	@Before
	public void setup() {
		transitionProvider = new TransitionProvider();
		transitionProvider.onCreate();
		ShadowContentResolver.registerProvider(TransitionProvider.AUTHORITY, transitionProvider);
		ContentValues location = new ContentValues();
		location.put("lat", 0);
		location.put("lon", 0);
		transitionProvider.update(TransitionProvider.LOCATION, location, null, null);
	}

    @Test
    public void shouldStartTicking() {
        ServiceController<JttService> controller = Robolectric.buildService(JttService.class);
        controller.attach().create().withIntent(new Intent(Robolectric.application, JttService.class)).startCommand(0, 0);

		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
    }

    private List<ScheduledAlarm> getScheduledAlarms() {
		AlarmManager am = (AlarmManager) Robolectric.application
				.getSystemService(Context.ALARM_SERVICE);
		return Robolectric.shadowOf(am).getScheduledAlarms();
    }

}
