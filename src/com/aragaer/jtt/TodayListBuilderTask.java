package com.aragaer.jtt;

import java.util.Collections;

import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

class TodayListBuilderTask extends AsyncTask<TodayInfo, TodayItem, Void> {
	private static final String TAG = TodayListBuilderTask.class.getSimpleName();

	final TodayAdapter adapter;
	final JttClient client;
	public TodayListBuilderTask(TodayAdapter adapter, JttClient client) {
		this.adapter = adapter;
		this.client = client;
	}

	protected Void doInBackground(TodayInfo... params) {
		try {
			final TodayInfo info = params[0];
			int pos;
			long now;
			while (true) { // we are likely to pass this loop only once
				final int len = info.transitions.size();

				if (len == 0 || (len % 2) == 1) {
					Log.i(TAG, "Transitions list is empty or has incorrect number of items");
					publishProgress();
					info.reset(client);
					continue;
				}

				now = System.currentTimeMillis();
				pos = Collections.binarySearch(info.transitions, now);

				/* current time falls between pos and pos+1 */
				if (pos < 0)
					pos = -pos - 2;

				/* check if we have enough data for past */
				if (pos < 1) {
					info.getPastDay(client);
					continue;
				}

				/* remove outdated stuff */
				/* magic arithmetics!
				 * in the end pos must be exactly 1 or 2
				 */
				final int past_days = (pos - 1) / 2;
				pos -= past_days * 2;
				info.drop_past_days(past_days);

				/* now pos is exactly 1 or 2
				 * 1 means it is night now
				 * 2 means it is day now
				 */

				/* check if we have enough data for future */
				if (len <= pos + 2) {
					info.getFutureDay(client);
					continue;
				}

				/* it is possible that we have too much "future" information
				 * keep it
				 */
				break;
			}

			/* exactly 4 transitions are used:
			 * pos-1, pos, pos+1 and pos+2
			 */
			final long[] l = new long[4];
			for (int i = 0; i < 4; i++)
				l[i] = info.transitions.get(pos - 1 + i);

			info.prev_transition = l[1];
			info.next_transition = l[2];

			/* if it is day now then first interval is night */
			int h_add = pos == 1 ? 6 : 0;

			/* start with first transition */
			publishProgress(new HourItem(l[0], h_add));
			for (int i = 1; i < l.length; i++) {
				long start = l[i - 1];
				long diff = l[i] - start;
				for (int j = 1; j <= 12; j++) {
					long t = start + j * diff / 12;
					final TodayItem item = j % 2 == 1
							? new BoundaryItem(t)
					: new HourItem(t, h_add + j / 2);
							publishProgress(item);
				}
				h_add = 6 - h_add;
			}
		} catch (NullPointerException e) {
			cancel(false);
		} catch (ArrayIndexOutOfBoundsException e) {
			cancel(false);
		} catch (RemoteException e) {
			Log.e(TAG, "Service connection lost: "+e);
			cancel(false);
		}
		return null;
	}

	protected void onProgressUpdate(TodayItem... items) {
		if (items == null)
			adapter.clear();
		else if (Build.VERSION.SDK_INT >= 11)
			adapter.addAll(items);
		else for (TodayItem item : items)
			adapter.add(item);
	}

	protected void onPreExecute() {
		adapter.clear();
	}

	protected void onPostExecute(Void result) {
		adapter.mark_current();
	}
}
