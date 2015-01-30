package com.chinaxing.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 用来跟踪bean的生命周期
 * Created by lenovo on 2015/1/29.
 */
public class ChinaBeanInfo {
    private String beanName;
    private boolean isFactoryBean = false;
    private Method factoryMethod = null;
    private Class<?> beanClass;
    private Object beanInstance;
    private BeanState state = BeanState.INITIAL;
    private List<Field> injectedFields = new ArrayList<Field>();
    private Constructor constructor;
    private List<Field> unInjectedFields = new ArrayList<Field>();
    private List<Method> hooks = new ArrayList<Method>();
    private Class objectType;

    public String getBeanName() {
        if (beanName == null) beanName = beanClass.getName();
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public boolean isFactoryBean() {
        return isFactoryBean;
    }

    public void setFactoryBean(boolean isFactoryBean) {
        this.isFactoryBean = isFactoryBean;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

    public void setFactoryMethod(Method factoryMethod) {
        this.factoryMethod = factoryMethod;
    }

    public Constructor getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor constructor) {
        this.constructor = constructor;
    }

    public ChinaBeanInfo(Class<?> beanClass) {
        this.beanClass = beanClass;
        this.beanName = beanClass.getName();
    }

    public ChinaBeanInfo(String beanName, Class<?> beanClass) {
        this.beanName = beanName;
        this.beanClass = beanClass;
    }

    public ChinaBeanInfo() {
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public Object getBeanInstance() {
        return beanInstance;
    }

    public void setBeanInstance(Object beanInstance) {
        this.beanInstance = beanInstance;
    }

    public BeanState getState() {
        return state;
    }

    public void setState(BeanState state) {
        this.state = state;
    }

    public List<Field> getInjectedFields() {
        return injectedFields;
    }

    public void setInjectedFields(List<Field> injectedFields) {
        this.injectedFields = injectedFields;
    }

    public List<Field> getUnInjectedFields() {
        return unInjectedFields;
    }

    public void setUnInjectedFields(List<Field> unInjectedFields) {
        this.unInjectedFields = unInjectedFields;
    }

    public List<Method> getHooks() {
        return hooks;
    }

    public void setHooks(List<Method> hooks) {
        this.hooks = hooks;
    }

    public void setObjectType(Class objectType) {
        this.objectType = objectType;
    }

    public Class getObjectType() {
        return objectType;
    }
}
