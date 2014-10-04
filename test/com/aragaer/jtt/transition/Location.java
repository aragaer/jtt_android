package com.aragaer.jtt.transition;

import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface Location {
	double latitude();
	double longitude();
}
