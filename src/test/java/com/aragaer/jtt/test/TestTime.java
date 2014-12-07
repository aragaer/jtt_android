package com.aragaer.jtt.test;
// vim: et ts=4 sts=4 sw=4

import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface TestTime {
    int year();
    int month();
    int day();
    int hour() default 0;
    int minute() default 0;
    int second() default 0;
}
