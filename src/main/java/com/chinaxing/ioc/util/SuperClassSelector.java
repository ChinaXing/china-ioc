package com.chinaxing.ioc.util;

import java.lang.reflect.Modifier;

/**
 * Created by lenovo on 2015/1/29.
 */
public class SuperClassSelector implements ClassSelector {
    private Class superClass;

    public SuperClassSelector(Class superClass) {
        this.superClass = superClass;
    }

    @Override
    public boolean select(Class c) {
        return superClass.isAssignableFrom(c)
                && !Modifier.isInterface(c.getModifiers())
                && !Modifier.isAbstract(c.getModifiers());
    }
}
