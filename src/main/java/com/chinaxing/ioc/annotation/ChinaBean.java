package com.chinaxing.ioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lenovo on 2015/1/29.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChinaBean {
    ChinaBeanType type() default ChinaBeanType.NORMAL_BEAN;

    String value() default "";
}
