package com.chinaxing.ioc;

import com.chinaxing.ioc.util.CollectionUtil;
import com.chinaxing.ioc.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by lenovo on 2015/1/29.
 */
public class BeanContainer {
    private static final Logger logger = LoggerFactory.getLogger(BeanContainer.class);
    Map<Class, List<ChinaBeanInfo>> classBeans = new HashMap<Class, List<ChinaBeanInfo>>();
    Map<String, ChinaBeanInfo> namedBeans = new HashMap<String, ChinaBeanInfo>();

    public <T> void registerBean(Class<T> tClass, T object, boolean expand, boolean isFactoryBean, Method factoryMethod, Class objectType) {
        ChinaBeanInfo chinaBeanInfo = new ChinaBeanInfo(tClass);
        chinaBeanInfo.setBeanInstance(object);
        chinaBeanInfo.setFactoryBean(isFactoryBean);
        chinaBeanInfo.setFactoryMethod(factoryMethod);
        chinaBeanInfo.setObjectType(objectType);
        chinaBeanInfo.setState(BeanState.HOOKED);
        CollectionUtil.addMapList(classBeans, tClass, chinaBeanInfo);
        namedBeans.put(tClass.getName(), chinaBeanInfo);
        if (expand) {
            expandContainer(tClass, chinaBeanInfo);
        }

        if (isFactoryBean) {
            CollectionUtil.addMapList(classBeans, objectType, chinaBeanInfo);
            namedBeans.put(objectType.getName(), chinaBeanInfo);
        }
    }

    public <T> List<T> getBeansOfType(Class<T> tClass) {
        List<T> result = new ArrayList<T>();
        List<ChinaBeanInfo> beans = classBeans.get(tClass);
        if (beans == null) return result;
        for (ChinaBeanInfo chinaBeanInfo : beans) {
            if (chinaBeanInfo.isFactoryBean() && tClass == chinaBeanInfo.getObjectType()) {
                Method m = chinaBeanInfo.getFactoryMethod();
                if (m.getParameterCount() == 0) {
                    try {
                        T r = (T) m.invoke(chinaBeanInfo.getBeanInstance());
                        result.add(r);
                    } catch (Exception e) {
                        logger.error("create bean of : {} by factory method : {} ", chinaBeanInfo, m, e);
                    }
                }
            } else {
                result.add((T) chinaBeanInfo.getBeanInstance());
            }
        }
        return result;
    }

    public <T> List<T> getInjectedBeansOfType(Class<T> tClass) {
        List<T> result = new ArrayList<T>();
        List<ChinaBeanInfo> beans = classBeans.get(tClass);
        if (beans == null) return result;
        for (ChinaBeanInfo chinaBeanInfo : beans) {
            if (chinaBeanInfo.getState().equals(BeanState.INJECTED) || chinaBeanInfo.getState().equals(BeanState.HOOKED)) {
                if (chinaBeanInfo.isFactoryBean() && tClass == chinaBeanInfo.getObjectType()) {
                    Method m = chinaBeanInfo.getFactoryMethod();
                    if (m.getParameterCount() == 0) {
                        try {
                            T r = (T) m.invoke(chinaBeanInfo.getBeanInstance());
                            result.add(r);
                        } catch (Exception e) {
                            logger.error("create bean of : {} by factory method : {} ", chinaBeanInfo, m, e);
                        }
                    }
                } else {
                    result.add((T) chinaBeanInfo.getBeanInstance());
                }
            }
        }
        return result;
    }

    public <T> List<T> getBeansOfType(Class<T> tClass, Object... args) {
        List<T> result = new ArrayList<T>();
        List<ChinaBeanInfo> beans = classBeans.get(tClass);
        if (beans == null) return result;
        for (ChinaBeanInfo chinaBeanInfo : beans) {
            if (chinaBeanInfo.isFactoryBean() && tClass == chinaBeanInfo.getObjectType()) {
                Method m = chinaBeanInfo.getFactoryMethod();
                if (m.getParameterCount() == 0) {
                    try {
                        T r = (T) m.invoke(chinaBeanInfo.getBeanInstance(), args);
                        result.add(r);
                    } catch (Exception e) {
                        logger.error("create bean of : {} by factory method : {} ", chinaBeanInfo, m, e);
                    }
                }
            }
        }
        return result;
    }

    public <T> T getBeanOfType(Class<T> tClass) throws BeanNotUniqueException {
        List<ChinaBeanInfo> beans = classBeans.get(tClass);
        if (beans == null) return null;
        if (beans.size() != 1) {
            throw new BeanNotUniqueException("bean count " + beans.size());
        }
        if (beans.isEmpty()) return null;
        ChinaBeanInfo beanInfo = beans.get(0);
        if (beanInfo.isFactoryBean() && tClass == beanInfo.getObjectType()) {
            Method m = beanInfo.getFactoryMethod();
            try {
                return (T) m.invoke(beanInfo.getBeanInstance());
            } catch (Exception e) {
                logger.error("initial :{} by factory method failed : ", beanInfo, e);
                return null;
            }
        } else {
            return (T) beanInfo.getBeanInstance();
        }
    }

    public <T> T getInjectedBeanOfType(Class<T> tClass) throws BeanNotUniqueException {
        List<ChinaBeanInfo> beans = classBeans.get(tClass);
        if (beans == null) return null;
        if (beans.size() != 1) {
            throw new BeanNotUniqueException("bean count " + beans.size());
        }
        if (beans.isEmpty()) return null;
        ChinaBeanInfo beanInfo = beans.get(0);
        if (beanInfo.getState() != BeanState.HOOKED && beanInfo.getState() != BeanState.INJECTED) return null;
        if (beanInfo.isFactoryBean() && tClass == beanInfo.getObjectType()) {
            Method m = beanInfo.getFactoryMethod();
            try {
                return (T) m.invoke(beanInfo.getBeanInstance());
            } catch (Exception e) {
                logger.error("initial :{} by factory method failed : ", beanInfo, e);
                return null;
            }
        } else {
            return (T) beanInfo.getBeanInstance();
        }
    }

    public <T> T getBeanOfType(Class<T> tClass, Object... args) throws BeanNotUniqueException {
        List<ChinaBeanInfo> beans = classBeans.get(tClass);
        if (beans.size() != 1) {
            throw new BeanNotUniqueException("bean count " + beans.size());
        }
        if (beans.isEmpty()) return null;
        ChinaBeanInfo beanInfo = beans.get(0);
        if (beanInfo.isFactoryBean() && tClass == beanInfo.getObjectType()) {
            Method m = beanInfo.getFactoryMethod();
            try {
                return (T) m.invoke(beanInfo.getBeanInstance(), args);
            } catch (Exception e) {
                logger.error("initial :{} by factory method failed : ", beanInfo, e);
                return null;
            }
        } else {
            return (T) beanInfo.getBeanInstance();
        }
    }

    public <T> void registerBean(Class<T> tClass, T object) {
        registerBean(tClass, object, false, false, null, null);
    }

    public <T> void registerBeanExpand(Class<T> tClass, T object) {
        registerBean(tClass, object, true, false, null, null);
    }

    public <T> void registerFactoryBean(Class<T> tClass, T object, Method factoryMethod, Class objectType) {
        registerBean(tClass, object, false, true, factoryMethod, objectType);
    }

    public void registerBean(ChinaBeanInfo beanInfo) {
        CollectionUtil.addMapList(classBeans, beanInfo.getBeanClass(), beanInfo);
        namedBeans.put(beanInfo.getBeanName(), beanInfo);
        if (beanInfo.isFactoryBean()) {
            CollectionUtil.addMapList(classBeans, beanInfo.getObjectType(), beanInfo);
            namedBeans.put(beanInfo.getObjectType().getName(), beanInfo);
        } else {
            expandContainer(beanInfo.getBeanClass(), beanInfo);
        }
    }

    private void expandContainer(Class c, ChinaBeanInfo beanInfo) {
        Set<Class<?>> classes = flatSupperClass(c);
        for (Class clz : classes) {
            CollectionUtil.addMapList(classBeans, clz, beanInfo);
            namedBeans.put(clz.getName(), beanInfo);
        }
    }

    private Set<Class<?>> flatSupperClass(Class c) {
        Set<Class<?>> classes = ReflectionUtil.getSuperClasses(c);
        Set<Class<?>> interfaces = ReflectionUtil.getInterfaces(c);
        classes.addAll(interfaces);
        return classes;
    }
}
