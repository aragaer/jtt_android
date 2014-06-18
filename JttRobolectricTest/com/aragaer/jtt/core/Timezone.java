package com.aragaer.jtt.core;

import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
@interface Timezone {
	int offsetMinutes();
}