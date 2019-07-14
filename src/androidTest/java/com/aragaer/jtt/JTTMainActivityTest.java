// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;


import android.content.*;
import android.preference.PreferenceManager;
import android.support.test.uiautomator.*;

import androidx.test.espresso.*;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.*;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.AllOf.allOf;

@LargeTest
public class JTTMainActivityTest {

    @Rule
    public final ActivityTestRule<JTTMainActivity> mActivityRule = new ActivityTestRule<>(JTTMainActivity.class, true, false);

    private static final int LAUNCH_TIMEOUT = 5000;

    private void setInitialLocation(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(Settings.PREF_LOCATION, "0.0:0.0");
        editor.putString(Settings.PREF_LOCALE, "1");
        editor.commit();
    }

    @Before public void setUp() {
        Context context = getInstrumentation().getTargetContext();
        setInitialLocation(context);
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.pressHome();
        mActivityRule.launchActivity(new Intent());
        device.wait(Until.hasObject(By.pkg("com.aragaer.jtt").depth(0)), LAUNCH_TIMEOUT);
    }

    @Test public void testSwiping() {
        ViewInteraction viewPager = onView(allOf(withClassName(is("androidx.viewpager.widget.ViewPager")),
                                                 isDisplayed()));
        viewPager.perform(swipeLeft());
        viewPager.perform(swipeRight());
        viewPager.perform(swipeLeft());
        viewPager.perform(swipeRight());
        viewPager.perform(swipeLeft());
        viewPager.perform(swipeRight());
        viewPager.perform(swipeRight());
    }

    @Test public void testLanguage() {
        onView(withText("Clock")).check(matches(isDisplayed()));
        onView(withText("Today")).check(matches(isDisplayed()));

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(allOf(withId(android.R.id.title), withText("Settings"), isDisplayed())).perform(click());

        onData(hasToString(startsWith("Language"))).perform(click());
        onData(hasToString(startsWith("日本語"))).perform(click());
        androidx.test.espresso.Espresso.pressBack();

        onView(withText("今日")).check(matches(isDisplayed()));
        onView(withText("時計")).check(matches(isDisplayed()));

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(android.R.id.title), withText("設定"), isDisplayed())).perform(click());
        onData(hasToString(startsWith("言語"))).perform(click());

        onData(hasToString(startsWith("Русский"))).perform(click());
        androidx.test.espresso.Espresso.pressBack();

        onView(withText("Часы")).check(matches(isDisplayed()));
        onView(withText("Сегодня")).check(matches(isDisplayed()));
    }

    @Test public void testSetStyle() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(android.R.id.title), withText("Settings"), isDisplayed())).perform(click());
        onData(hasToString(startsWith("Theme"))).perform(click());
        onView(allOf(withId(android.R.id.text1), withText("Translucent"))).check(matches(isDisplayed()));
        onView(allOf(withId(android.R.id.text1), withText("Light"))).check(matches(isDisplayed()));
        onView(allOf(withId(android.R.id.text1), withText("Dark"), isDisplayed())).perform(click());
        androidx.test.espresso.Espresso.pressBack();
    }

    @Test public void testSetLocation() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(android.R.id.title), withText("Settings"), isDisplayed())).perform(click());
        onData(hasToString(startsWith("Location"))).perform(click());
        onData(hasToString(startsWith("Change stored location"))).perform(click());
        onView(allOf(withId(R.id.lat), withText("0.0"), isDisplayed())).perform(replaceText("55.78"));
        onView(allOf(withId(R.id.lon), withText("0.0"), isDisplayed())).perform(replaceText("37.65"));
        onView(withText("Cancel")).check(matches(isDisplayed()));
        onView(withText("Use current location")).check(matches(isDisplayed()));
        onView(withText("OK")).perform(click());
        String locationValue = PreferenceManager
            .getDefaultSharedPreferences(getInstrumentation().getTargetContext())
            .getString(Settings.PREF_LOCATION, "XXX");
        assertThat(locationValue, equalTo("55.78:37.65"));
        androidx.test.espresso.Espresso.pressBack();
    }
}
