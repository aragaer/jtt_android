package com.aragaer.jtt.test;

import com.aragaer.jtt.JTTMainActivity;
import com.aragaer.jtt.R;
import org.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    @Test
    public void appName() throws Exception {
        String app_name = new JTTMainActivity().getResources().getString(R.string.app_name);
        assertThat(app_name, equalTo("Japanese Traditional Time"));
    }
}