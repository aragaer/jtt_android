package com.aragaer.jtt;

import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    MainActivity activity;

    @Before
    public void setup() {
        this.activity = Robolectric.buildActivity(MainActivity.class).create().get();
    }

    @Test
    public void shouldHaveApplicationName() throws Exception {
        String app_name = this.activity.getString(R.string.app_name);
        assertThat(app_name, equalTo("Japanese Traditional Time"));
    }
}