package com.aragaer.jtt.test;

import java.lang.reflect.Field;

import android.content.ContentValues;

import com.aragaer.jtt.core.Calculator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CalculatorTest {
	private Calculator calc_init(Calculator calc) {
		ContentValues location = new ContentValues(2);
		location.put("lat", "0.0");
		location.put("lon", "0.0");
		calc.update(Calculator.LOCATION, location, null, null);
		return calc;
	}

	@Test
	public void calculatorInit() throws Exception {
		Field calc_field = Calculator.class.getDeclaredField("calculator");
		calc_field.setAccessible(true);

		Calculator calc = new Calculator();
		assertThat(calc_field.get(calc), nullValue());
		calc_init(calc);

		assertThat(calc_field.get(calc), notNullValue());
	}
}
