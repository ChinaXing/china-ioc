package com.chinaxing.ioc;

import com.chinaxing.ioc.annotation.*;
import com.chinaxing.ioc.util.AnnotationClassSelector;
import com.chinaxing.ioc.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;

/**
 * 要解决的问题：
 * 1. 一个bean 的初始化程度：
 * a. 实例创建完成
 * b. 注入未进行
 * c. 部分注入完成 需要记录哪些字段注入完成，哪些没有
 * d. 注入完成
 * e. 相关hook执行完成
 * <p/>
 * 2.注入的时候，必须确保被注入的bean已经可用（state = INJECTED）
 * <p/>
 * 1. 初始化默认构造的类
 * 2. 构造完成，但是没有执行注入的对象，执行注入
 * 3. 加入完成集合：构造完成，且执行完注入
 * 4. 执行afterInit
 * Ioc 容器
 * Created by lenovo on 2015/1/29.
 */
public class ChinaIOC {
    private final static Logger logger = LoggerFactory.getLogger(ChinaIOC.class);

    /**
     * 初始状态 ，还未创建
     */
    private Map<Class, ChinaBeanInfo> initialBeanMap = new HashMap<Class, ChinaBeanInfo>();
    /**
     * 已经创建实例，但是还未进行注入
     */
    private Map<Class, ChinaBeanInfo> instantBeanMap = new HashMap<Class, ChinaBeanInfo>();
    /**
     * 注入完成，但是还未进行调用hook
     */
    private Map<Class, ChinaBeanInfo> injectedBeanMap = new HashMap<Class, ChinaBeanInfo>();

    private BeanContainer beanContainer = new BeanContainer();

    private Properties properties;

    public <T> T getChinaBean(Class<T> c) throws BeanNotUniqueException {
        return beanContainer.getBeanOfType(c);
    }

    public <T> T getChinaBean(Class<T> c, Object... args) throws BeanNotUniqueException {
        return beanContainer.getBeanOfType(c, args);
    }

    public <T> List<T> getChinaBeansOfType(Class<T> tClass) {
        return beanContainer.getBeansOfType(tClass);
    }

    public ChinaIOC(String basePackage, Properties properties) throws InitializeBeanException {
        this.properties = properties;
        List<Class<?>> pkgClassList = ReflectionUtil.listPackageClass(basePackage, new AnnotationClassSelector<ChinaBean>(ChinaBean.class));
        for (Class c : pkgClassList) {
            ChinaBeanInfo beanInfo = new ChinaBeanInfo(c);
            initialBeanInfo(beanInfo);
            initialBeanMap.put(c, beanInfo);
        }
    }

    public <T> void addBean(Class<T> clz, T object) {
        beanContainer.registerBean(clz, object);
    }

    public void initContainer() throws InitializeBeanException {
        preInitialClass();
        for (ChinaBeanInfo beanInfo : injectedBeanMap.values()) {
            doAfterInitial(beanInfo);
            beanInfo.setState(BeanState.HOOKED);
        }
    }


    private void initialBeanInfo(ChinaBeanInfo beanInfo) throws InitializeBeanException {
        Class c = beanInfo.getBeanClass();
        beanInfo.setUnInjectedFields(ReflectionUtil.getFieldWithAnnotation(c, Inject.class));
        beanInfo.setHooks(ReflectionUtil.getMethodWithAnnotation(c, AfterInit.class));
        beanInfo.setConstructor(ReflectionUtil.getUniqueConstructor(c));
        ChinaBean cb = (ChinaBean) c.getAnnotation(ChinaBean.class);
        String beanName = cb.value();
        if (beanName != null && !beanName.isEmpty()) {
            beanInfo.setBeanName(beanName);
        }
        if (cb.type() == ChinaBeanType.FACTORY_BEAN) {
            beanInfo.setFactoryBean(true);
            List<Method> methods = ReflectionUtil.getMethodWithAnnotation(c, ChinaBeanFactoryMethod.class);
            if (methods.isEmpty())
                throw new InitializeBeanException("Factory Bean has no FactoryMethod : " + c.getClass().getName());
            beanInfo.setFactoryMethod(methods.get(0));
            methods = ReflectionUtil.getMethodWithAnnotation(c, ChinaBeanObjectType.class);
            if (methods.isEmpty()) {
                throw new InitializeBeanException("Factory Bean need @ChinaBeanObjectType annotation " + c.getClass().getName());
            }
            Method m = methods.get(0);
            if (!Modifier.isStatic(m.getModifiers())) {
                throw new InitializeBeanException("@ChinaBeanObjectType need static method" + c.getClass().getName());
            }
            try {
                beanInfo.setObjectType((Class) m.invoke(null));
            } catch (Exception e) {
                throw new InitializeBeanException(e);
            }
        }
    }


    private void preInitialClass() throws InitializeBeanException {
        int beforeSize = injectedBeanMap.size();
        /**
         * 实例化bean
         */
        Iterator<Class> iterator = initialBeanMap.keySet().iterator();
        while (iterator.hasNext()) {
            Class c = iterator.next();
            ChinaBeanInfo beanInfo = initialBeanMap.get(c);
            Object instance = instantBean(beanInfo);
            if (instance != null) {
                beanInfo.setBeanInstance(instance);
                beanInfo.setState(BeanState.INSTANT);
                instantBeanMap.put(c, beanInfo);
                beanContainer.registerBean(beanInfo);
                iterator.remove();
            }
        }
        /**
         * 注入bean
         */
        iterator = instantBeanMap.keySet().iterator();
        while (iterator.hasNext()) {
            Class c = iterator.next();
            ChinaBeanInfo beanInfo = instantBeanMap.get(c);
            boolean t = injectBean(beanInfo);
            if (t) {
                beanInfo.setState(BeanState.INJECTED);
                injectedBeanMap.put(c, beanInfo);
                iterator.remove();
            }
        }

        if (beforeSize == injectedBeanMap.size()) {
            throw new InitializeBeanException("cannot init container ...");
        }
        if (initialBeanMap.isEmpty() && instantBeanMap.isEmpty()) {
            logger.info("initialize container succeed !");
        } else {
            preInitialClass();
        }
    }

    private void doAfterInitial(ChinaBeanInfo beanInfo) throws InitializeBeanException {
        Object instance = beanInfo.getBeanInstance();
        for (Method m : beanInfo.getHooks()) {
            if (m.getParameterCount() != 0)
                throw new InitializeBeanException("@AfterInit method must have zero arguments :" + m.getName());
            try {
                m.setAccessible(true);
                m.invoke(instance);
            } catch (Throwable t) {
                throw new InitializeBeanException(t);
            }
        }
    }

    private boolean injectBean(ChinaBeanInfo beanInfo) throws InitializeBeanException {
        Object instance = beanInfo.getBeanInstance();
        List<Field> unInjectedFields = beanInfo.getUnInjectedFields();
        List<Field> injectedFields = beanInfo.getInjectedFields();
        for (Field f : unInjectedFields) {
            Inject inject = f.getAnnotation(Inject.class);
            if (inject.property()) {
                try {
                    ReflectionUtil.setFieldByProperty(instance, f, properties.get(inject.propertyName()));
                    injectedFields.add(f);
                    continue;
                } catch (Throwable t) {
                    throw new InitializeBeanException(t);
                }
            }
            Object injectMember;
            if (Collection.class.isAssignableFrom(f.getType())) {
                List<Object> classes = (List<Object>) getChinaBeansOfType(f.getType());
                if (List.class.isAssignableFrom(f.getType())) {
                    injectMember = classes;
                } else if (Set.class.isAssignableFrom(f.getType())) {
                    Set<Object> it = new HashSet();
                    it.addAll(classes);
                    injectMember = it;
                } else {
                    throw new InitializeBeanException("current only support Set and List collection type");
                }
            } else {
                try {
                    injectMember = getChinaBean(f.getType());
                } catch (BeanNotUniqueException e) {
                    throw new InitializeBeanException(e);
                }
            }
            if (injectMember != null) {
                try {
                    f.setAccessible(true);
                    f.set(instance, injectMember);
                    injectedFields.add(f);
                } catch (Exception e) {
                    throw new InitializeBeanException(e);
                }
            }
        }
        unInjectedFields.removeAll(injectedFields);
        if (unInjectedFields.isEmpty()) return true;
        return false;
    }

    private Object instantBean(ChinaBeanInfo beanInfo) throws InitializeBeanException {
        Constructor c = beanInfo.getConstructor();
        /**
         * 无参数构造
         */
        if (c.getParameterCount() == 0) {
            try {
                return c.newInstance();
            } catch (Exception e) {
                throw new InitializeBeanException(e);
            }
        }

        if (!c.isAnnotationPresent(Inject.class)) {
            logger.error("constructor with arguments must have @Inject annotation !");
        }

        Parameter[] parameters = c.getParameters();
        boolean parameterHasMissed = false;
        Object[] args = new Object[parameters.length];
        int i = 0;
        for (Parameter p : parameters) {
            try {
                Object o = getInjectedChinaBean(p.getType());
                if (o == null) {
                    logger.debug("parameter : {} no instance, initial later !", p);
                    parameterHasMissed = true;
                    break;
                }
                args[i] = o;
                i++;
            } catch (Exception e) {
                throw new InitializeBeanException(e);
            }
        }

        if (!parameterHasMissed) {
            try {
                return c.newInstance(args);
            } catch (Exception e) {
                logger.error("", e);
                throw new InitializeBeanException(e);
            }
        }

        return null;
    }

    private <T> T getInjectedChinaBean(Class<T> type) throws BeanNotUniqueException {
        return beanContainer.getInjectedBeanOfType(type);
    }

    private <T> List<T> getChinaInjectedBeansOfType(Class<T> type) {
        return beanContainer.getInjectedBeansOfType(type);
    }
}
