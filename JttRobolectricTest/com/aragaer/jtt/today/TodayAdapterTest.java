package com.aragaer.jtt.today;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import com.aragaer.jtt.core.ThreeIntervals;

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

	private ThreeIntervals timestampsAroundNowWithOffset(long offset) {
		long now = System.currentTimeMillis();
		return new ThreeIntervals(new long[] { now - 1200 + offset, now - 600 + offset,
				now + offset, now + 600 + offset }, true);
	}

	@Test
	public void shouldHaveFullSetAfterTick() {
		adapter.setTransitions(timestampsAroundNowWithOffset(400));
		assertThat(adapter.getCount(), equalTo(37));
	}

	@Test
	public void shouldBeEmptyWhenDataIsStale() {
		adapter.setTransitions(timestampsAroundNowWithOffset(-400));
		assertThat(adapter.getCount(), equalTo(0));
	}

	@Test
	public void shouldBeEmptyWhenDataIsVeryStale() {
		adapter.setTransitions(timestampsAroundNowWithOffset(-1000));
		assertThat(adapter.getCount(), equalTo(0));
	}

	@Test
	public void shouldBeEmptyWhenDataIsInFuture() {
		adapter.setTransitions(timestampsAroundNowWithOffset(800));
		assertThat(adapter.getCount(), equalTo(0));
	}

	@Test
	public void shouldBeEmptyWhenDataIsFarInFuture() {
		adapter.setTransitions(timestampsAroundNowWithOffset(1400));
		assertThat(adapter.getCount(), equalTo(0));
	}
}
