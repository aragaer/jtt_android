package com.aragaer.jtt.test;

import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface TestTimezone {
	int offsetMinutes();
}
