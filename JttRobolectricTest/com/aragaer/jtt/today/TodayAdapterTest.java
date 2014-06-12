package com.aragaer.jtt.today;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class TodayAdapterTest {
	private TodayAdapter adapter;

	@Before
	public void setup() {
		adapter = new TodayAdapter(Robolectric.application, 0);
	}

	@Test
	public void shouldBeEmptyBeforeTick() {
		assertThat(adapter.getCount(), equalTo(0));
	}

	private long[] timestampsAroundNowWithOffset(long offset) {
		long now = System.currentTimeMillis();
		return new long[] { now - 1200 + offset, now - 600 + offset,
				now + offset, now + 600 + offset };
	}

	@Test
	public void shouldHaveFullSetAfterTick() {
		adapter.setTimestamps(timestampsAroundNowWithOffset(400), true);
		assertThat(adapter.getCount(), equalTo(37));
	}

	@Test
	public void shouldBeEmptyWhenDataIsStale() {
		adapter.setTimestamps(timestampsAroundNowWithOffset(-400), true);
		assertThat(adapter.getCount(), equalTo(0));
	}

	@Test
	public void shouldBeEmptyWhenDataIsVeryStale() {
		adapter.setTimestamps(timestampsAroundNowWithOffset(-1000), true);
		assertThat(adapter.getCount(), equalTo(0));
	}

	@Test
	public void shouldBeEmptyWhenDataIsInFuture() {
		adapter.setTimestamps(timestampsAroundNowWithOffset(800), true);
		assertThat(adapter.getCount(), equalTo(0));
	}

	@Test
	public void shouldBeEmptyWhenDataIsFarInFuture() {
		adapter.setTimestamps(timestampsAroundNowWithOffset(1400), true);
		assertThat(adapter.getCount(), equalTo(0));
	}
}
