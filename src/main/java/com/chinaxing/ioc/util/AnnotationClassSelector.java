package com.chinaxing.ioc.util;

import java.lang.annotation.Annotation;

/**
 * Created by lenovo on 2015/1/29.
 */
public class AnnotationClassSelector<T extends Annotation> implements ClassSelector {
    private Class<T> annotationClass;

    public AnnotationClassSelector(Class<T> tClass) {
        annotationClass = tClass;
    }

    @Override
    public boolean select(Class c) {
        return c.isAnnotationPresent(annotationClass);
    }
}
