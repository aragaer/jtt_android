// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.resources;

import android.content.*;
import android.content.pm.*;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import org.junit.*;
import org.junit.runner.RunWith;

import com.aragaer.jtt.R;
import com.aragaer.jtt.Settings;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;


@RunWith(AndroidJUnit4.class)
public class StringResourcesTest {

    private Context context;
    private StringResources stringResources;
    private ChangeListener changeListener;

    @Before public void setUp() {
	context = getTargetContext();
	changeListener = new ChangeListener();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(Settings.PREF_LOCALE, "");
        editor.putBoolean(Settings.PREF_EMOJI_WIDGET, false);
        editor.commit();
	stringResources = new StringResources(context);
	stringResources.registerStringResourceChangeListener(changeListener,
							     StringResources.TYPE_HOUR_NAME | StringResources.TYPE_WIDGET_FORMAT);
    }

    @Test public void testEnglishHourNames() throws Exception {
	setLocale("en");
	String shortNames[] = "Cock Dog Boar Rat Ox Tiger Hare Dragon Serpent Horse Ram Monkey".split(" ");
	for (int i = 0; i < 12; i++) {
	    assertThat(stringResources.getHour(i), equalTo("The " + shortNames[i]));
	    assertThat(stringResources.getHrOf(i), equalTo("Hour of the " + shortNames[i]));
	}
    }

    @Test public void testRussianHourNames() throws Exception {
	setLocale("ru");
	String shortNames[] = "Петух Собака Кабан Крыса Бык Тигр Заяц Дракон Змея Лошадь Овца Обезьяна".split(" ");
	String longNames[] = ("Час Петуха/Час Собаки/Час Кабана/Час Крысы/Час Быка/Час Тигра/"+
			      "Час Зайца/Час Дракона/Час Змеи/Час Лошади/Час Овцы/Час Обезьяны").split("/");
	for (int i = 0; i < 12; i++) {
	    assertThat(stringResources.getHour(i), equalTo(shortNames[i]));
	    assertThat(stringResources.getHrOf(i), equalTo(longNames[i]));
	}
    }

    @Test public void testTextWidget() throws Exception {
	setWidgetHourFormat(true);
	setWidgetHourFormat(false);
	String shortNames[] = "Cock Dog Boar Rat Ox Tiger Hare Dragon Serpent Horse Ram Monkey".split(" ");
	for (int i = 0; i < 12; i++) {
	    assertThat(stringResources.getHour(i), equalTo("The " + shortNames[i]));
	    assertThat(stringResources.formatHourForWidget(i), equalTo("The " + shortNames[i]));
	}
    }

    @Test public void testEmojiWidget() throws Exception {
	setWidgetHourFormat(true);
	String shortNames[] = "Cock Dog Boar Rat Ox Tiger Hare Dragon Serpent Horse Ram Monkey".split(" ");
	String emoji[] = "🐓🐕🐖🐀🐂🐅🐇🐉🐍🐏🐎🐒".split("(?!^)");
	for (int i = 0; i < 12; i++) {
	    assertThat(stringResources.getHour(i), equalTo("The " + shortNames[i]));
	    assertThat(stringResources.formatHourForWidget(i), equalTo(emoji[i]));
	}
    }

    private void setLocale(String locale) throws Exception {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(Settings.PREF_LOCALE, locale);
        editor.commit();
	synchronized (changeListener) {
	    changeListener.wait(1000);
	}
	assertThat("StringResources hour name changed",
		   changeListener.triggered & StringResources.TYPE_HOUR_NAME,
		   equalTo(StringResources.TYPE_HOUR_NAME));
    }

    private void setWidgetHourFormat(boolean value) throws Exception {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(Settings.PREF_EMOJI_WIDGET, value);
        editor.commit();
	synchronized (changeListener) {
	    changeListener.wait(1000);
	}
	assertThat("StringResources hour name changed",
		   changeListener.triggered,
		   equalTo(StringResources.TYPE_WIDGET_FORMAT));
    }

    private class ChangeListener implements StringResources.StringResourceChangeListener {
        int triggered;

        @Override public void onStringResourcesChanged(final int changes) {
            triggered = changes;
            synchronized (this) {
                this.notify();
            }
        }
    }
}
