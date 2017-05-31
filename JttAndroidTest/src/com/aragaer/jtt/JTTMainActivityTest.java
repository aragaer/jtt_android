// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.test.espresso.*;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.MonitoringInstrumentation;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.*;

import org.hamcrest.*;
import org.junit.*;
import org.junit.runner.RunWith;

import android.support.test.uiautomator.*;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class JTTMainActivityTest {

    @Rule
    public ActivityTestRule<JTTMainActivity> mActivityRule = new ActivityTestRule<>(JTTMainActivity.class, true, false);

    private static final int LAUNCH_TIMEOUT = 5000;

    public void setInitialLocation(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(Settings.PREF_LOCATION, "0.0:0.0");
        editor.putString(Settings.PREF_LOCALE, "1");
        editor.commit();
    }

    @Before public void setUp() {
        Context context = getInstrumentation().getTargetContext();
        setInitialLocation(context);
        mActivityRule.launchActivity(new Intent());
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.wait(Until.hasObject(By.pkg("com.aragaer.jtt").depth(0)), LAUNCH_TIMEOUT);
    }

    @Test public void testSwiping() {
        ViewInteraction viewPager = onView(allOf(withClassName(is("android.support.v4.view.ViewPager")),
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
        onView(allOf(withId(android.R.id.text1), withText("日本語"), isDisplayed())).perform(click());
        android.support.test.espresso.Espresso.pressBack();

        onView(withText("今日")).check(matches(isDisplayed()));
        onView(withText("時計")).check(matches(isDisplayed()));

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(android.R.id.title), withText("設定"), isDisplayed())).perform(click());
        onData(hasToString(startsWith("言語"))).perform(click());

        onView(allOf(withId(android.R.id.text1), withText("Русский"), isDisplayed())).perform(click());
        android.support.test.espresso.Espresso.pressBack();

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
        android.support.test.espresso.Espresso.pressBack();
    }

    @Test public void testSetLocation() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(android.R.id.title), withText("Settings"), isDisplayed())).perform(click());
        onData(hasToString(startsWith("Change stored location"))).perform(click());
        onView(allOf(withId(R.id.lat), withText("0.0"), isDisplayed())).perform(replaceText("55.78"));
        onView(allOf(withId(R.id.lon), withText("0.0"), isDisplayed())).perform(replaceText("37.65"));
        onView(withText("Cancel")).check(matches(isDisplayed()));
        onView(withText("Use current location")).check(matches(isDisplayed()));
        onView(withText("OK")).perform(click());
        android.support.test.espresso.Espresso.pressBack();
    }

    private static Matcher<View> childAtPosition(final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                    && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}
