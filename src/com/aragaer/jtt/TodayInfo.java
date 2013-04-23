package com.aragaer.jtt;

import java.util.LinkedList;

import android.os.RemoteException;
import android.util.Log;

/* Holds the data about everything shown in the list
 */
final class TodayInfo {
	private static final String TAG = TodayInfo.class.getSimpleName();
	private long jdn_min, jdn_max;
	LinkedList<Long> transitions = new LinkedList<Long>();
	long prev_transition, next_transition;

	/* fetch transitions for given day from JTTService
	 * if list is empty at the moment, we have only one day
	 * that starts with sunrise and ends with sunset
	 * if list is not empty, we have a new night and a day
	 * these two go to the beginning or to the end of the list
	 */
	private void getDay(final JttClient client, final long jdn) throws RemoteException {
		final long tr[] = client.getTr(jdn);
		final long sunrise = tr[0];
		final long sunset = tr[1];

		if (transitions.isEmpty()) {
			transitions.add(sunrise);
			transitions.add(sunset);
			jdn_max = jdn_min = jdn;
		} else if (jdn_max < jdn) {
			transitions.add(sunrise);
			transitions.add(sunset);
			jdn_max = jdn;
		} else if (jdn_min > jdn) { // add to front
			transitions.addFirst(sunset);
			transitions.addFirst(sunrise);
			jdn_min = jdn;
		} else
			Log.e(TAG, "Got "+jdn+" which is between "+jdn_min+" and "+jdn_max);
	}

	public void reset(final JttClient client) throws RemoteException {
		transitions.clear();
		getDay(client, JTT.longToJDN(System.currentTimeMillis()));
	}

	public void getPastDay(final JttClient client) throws RemoteException {
		getDay(client, jdn_min - 1);
	}

	public void getFutureDay(final JttClient client) throws RemoteException {
		getDay(client, jdn_max + 1);
	}

	public void drop_past_days(final int count) {
		for (int i = 0; i < count * 2; i++)
			transitions.remove();
		jdn_min += count;
	}
}