package com.aragaer.jtt.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.Context;

import com.aragaer.jtt.today.TodayAdapter;
import com.aragaer.jtt.today.TodayItem;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class TodayAdapterTest {

	Field transitions_field, selected_field;
	Method buildItems_method;
	Class<?> HourItem, BoundaryItem;

	public TodayAdapterTest() throws Exception {
		transitions_field = TodayAdapter.class.getDeclaredField("transitions");
		transitions_field.setAccessible(true);

		selected_field = TodayAdapter.class.getDeclaredField("selected");
		selected_field.setAccessible(true);

		buildItems_method = TodayAdapter.class.getDeclaredMethod("buildItems", boolean.class);
		buildItems_method.setAccessible(true);

		HourItem = Class.forName("com.aragaer.jtt.today.HourItem");
		BoundaryItem = Class.forName("com.aragaer.jtt.today.BoundaryItem");
	}

	private static final int increase = 1000;
	private static final int hour = increase * 2;
	private static final int six_hours = hour * 6;

	@Test
	public void testBuildItems() throws Exception {
		Context ctx = Robolectric.getShadowApplication().getApplicationContext();
		TodayAdapter ta = new TodayAdapter(ctx, 0);
		long transitions[] = (long[]) transitions_field.get(ta);

		for (int i = 0; i < 4; i++)
			transitions[i] = i * six_hours;

		buildItems_method.invoke(ta, true);

		assertThat(ta.getCount(), equalTo(37));

		boolean expect_hour = true;
		long value = 0;
		for (int i = 0; i < 37; i++) {
			TodayItem item = ta.getItem(i);
			if (expect_hour)
				assertThat(item, instanceOf(HourItem));
			else
				assertThat(item, instanceOf(BoundaryItem));

			expect_hour = !expect_hour;

			assertThat(item.time, equalTo(value));
			value += increase;
		}
	}

	@Test
	public void testSelected() throws Exception {
		Context ctx = Robolectric.getShadowApplication().getApplicationContext();
		TodayAdapter ta = new TodayAdapter(ctx, 0);
		long transitions[] = (long[]) transitions_field.get(ta);

		for (int offset = 0; offset < six_hours; offset += increase / 100) {
			int expected = 12 + ((offset + increase) / hour) * 2;

			long now = System.currentTimeMillis();
			for (int j = -1; j < 3; j++)
				transitions[j + 1] = j * six_hours - offset + now;

			buildItems_method.invoke(ta, true);
			ta.tick();

			assertThat(selected_field.getInt(ta), equalTo(expected));

			buildItems_method.invoke(ta, false);
			ta.tick();

			assertThat(selected_field.getInt(ta), equalTo(expected));
		}
	}
}
