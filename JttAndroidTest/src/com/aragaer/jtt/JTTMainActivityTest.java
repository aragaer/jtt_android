// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import android.content.*;
import android.preference.PreferenceManager;
import androidx.test.espresso.*;
import androidx.test.rule.ActivityTestRule;
import androidx.test.filters.LargeTest;
import android.view.*;

import org.hamcrest.*;
import org.junit.*;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;


@LargeTest
public class JTTMainActivityTest {

    @Rule
    public ActivityTestRule<JTTMainActivity> mActivityRule = new ActivityTestRule<>(JTTMainActivity.class, true, false);

    @Before public void setInitialLocation() {
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(Settings.PREF_LOCATION, "0.0:0.0");
        editor.putString(Settings.PREF_LOCALE, "1");
        editor.commit();
        mActivityRule.launchActivity(new Intent());
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

        ViewInteraction languageItem = onView(allOf(childAtPosition(withId(android.R.id.list), 5), isDisplayed()));
        languageItem.perform(click());

        ViewInteraction nihongo = onView(allOf(withId(android.R.id.text1), withText("日本語"), isDisplayed()));
        nihongo.perform(click());
        androidx.test.espresso.Espresso.pressBack();

        onView(withText("今日")).check(matches(isDisplayed()));
        onView(withText("時計")).check(matches(isDisplayed()));

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(android.R.id.title), withText("設定"), isDisplayed())).perform(click());

        languageItem = onView(allOf(childAtPosition(withId(android.R.id.list), 5), isDisplayed()));
        languageItem.perform(click());

        ViewInteraction russian = onView(allOf(withId(android.R.id.text1), withText("Русский"), isDisplayed()));
        russian.perform(click());
        androidx.test.espresso.Espresso.pressBack();

        onView(withText("Часы")).check(matches(isDisplayed()));
        onView(withText("Сегодня")).check(matches(isDisplayed()));

    }

    @Test public void testSetStyle() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(android.R.id.title), withText("Settings"), isDisplayed())).perform(click());
        ViewInteraction styleItem = onView(allOf(childAtPosition(withId(android.R.id.list), 8), isDisplayed()));
        styleItem.perform(click());
        ViewInteraction darkStyle = onView(allOf(withId(android.R.id.text1), withText("Dark"), isDisplayed()));
        darkStyle.perform(click());
        androidx.test.espresso.Espresso.pressBack();
    }

    @Test public void testSetLocation() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(android.R.id.title), withText("Settings"), isDisplayed())).perform(click());
        ViewInteraction locationItem = onView(allOf(childAtPosition(withId(android.R.id.list), 3), isDisplayed()));
        locationItem.perform(click());

        ViewInteraction editTextLat = onView(allOf(withId(R.id.lat), withText("0.0"), isDisplayed()));
        editTextLat.perform(replaceText("55.78"));

        ViewInteraction editTextLon = onView(allOf(withId(R.id.lon), withText("0.0"), isDisplayed()));
        editTextLon.perform(replaceText("37.65"));

        onView(allOf(withId(android.R.id.button1), withText("OK"), isDisplayed())).perform(click());

        androidx.test.espresso.Espresso.pressBack();
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

    private static ViewAction waitFor(final long millis) {
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
