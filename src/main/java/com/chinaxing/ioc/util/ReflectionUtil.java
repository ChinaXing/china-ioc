package com.chinaxing.ioc.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by lenovo on 2015/1/29.
 */
public class ReflectionUtil {
    public static Type getParameterType(Object o, String parameter) {
        Class currentClass = o.getClass();
        TypeVariable[] typeVariables = currentClass.getTypeParameters();
        Type genericType = currentClass.getGenericSuperclass();
        int index = 0;
        for (TypeVariable t : typeVariables) {
            if (t.getTypeName().equals(parameter)) {
                Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
                return actualTypes[index];
            }
            index += 1;
        }
        return null;
    }

    public static <T> List<Class<? extends T>> listPackageClass(String pkgName, ClassSelector selector) {
        List<Class<? extends T>> result = new ArrayList<Class<? extends T>>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(pkgName.replace(".", "/"));
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equalsIgnoreCase("jar")) {
                    JarURLConnection urlConnection = (JarURLConnection) resource.openConnection();
                    JarFile jarFile = urlConnection.getJarFile();
                    String prefix = urlConnection.getEntryName();
                    Enumeration<JarEntry> entryEnumeration = jarFile.entries();
                    while (entryEnumeration.hasMoreElements()) {
                        JarEntry entry = entryEnumeration.nextElement();
                        if (entry.isDirectory()) continue;
                        if (!entry.getName().startsWith(prefix)) continue;
                        if (entry.getName().endsWith(".class")) {
                            String className = entry.getName().substring(0, entry.getName().length() - 6).replace("/", ".");
                            loadClass(className, result, selector);
                        }
                    }
                    continue;
                }
                File f = new File(resource.getFile());
                if (!f.isDirectory()) { // should not be file , but a directory
                    continue;
                }
                for (File file : f.listFiles()) {
                    if (file.isFile()) {
                        String classFileName = file.getName();
                        if (file.getName().endsWith(".class")) {
                            String className = classFileName.substring(0, classFileName.length() - 6);
                            loadClass(pkgName + "." + className, result, selector);
                        }
                        continue;
                    }
                    if (file.isDirectory()) {
                        List<Class<? extends T>> classes = listPackageClass(pkgName + "." + file.getName(), selector);
                        result.addAll(classes);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    private static <T> void loadClass(String className, List<Class<? extends T>> result, ClassSelector selector) {
        try {
            Class c = Class.forName(className);
            if (selector.select(c)) {
                result.add(c);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public static boolean hasDefaultConstructor(Class c) {
        Constructor[] constructors = c.getDeclaredConstructors();
        if (constructors == null) return false;
        for (Constructor cst : constructors) {
            if (cst.getParameterCount() == 0) return true;
        }
        return false;
    }

    public static Constructor getUniqueConstructor(Class c) {
        Constructor[] constructors = c.getDeclaredConstructors();
        if (constructors == null) return null;
        if (constructors.length == 1) return constructors[0];
        return null;
    }

    public static boolean hasFieldAnnotation(Class c, Class<? extends Annotation> annotationClass) {
        Field[] fields = c.getDeclaredFields();
        if (fields != null) {
            for (Field f : fields) {
                if (f.isAnnotationPresent(annotationClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<Field> getFieldWithAnnotation(Class c, Class<? extends Annotation> annotationClass) {
        List<Field> result = new ArrayList<Field>();
        Field[] fields = c.getDeclaredFields();
        if (fields != null) {
            for (Field f : fields) {
                if (f.isAnnotationPresent(annotationClass)) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    public static <T extends Annotation> List<Method> getMethodWithAnnotation(Class c, Class<T> annotationClass) {
        List<Method> result = new ArrayList<Method>();
        Method[] methods = c.getDeclaredMethods();
        if (methods != null) {
            for (Method m : methods) {
                if (m.isAnnotationPresent(annotationClass)) result.add(m);
            }
        }
        return result;
    }

    public static Set<Class<?>> getSuperClasses(Class c) {
        Set<Class<?>> result = new LinkedHashSet<Class<?>>();
        do {
            Class s = c.getSuperclass();
            result.add(s);
            if (s == Object.class) {
                break;
            }
            c = s;
        } while (true);
        return result;
    }

    public static Set<Class<?>> getInterfaces(Class c) {
        Set<Class<?>> result = new LinkedHashSet<Class<?>>();
        Class<?>[] interfaces = c.getInterfaces();
        if (interfaces.length == 0) return result;
        result.addAll(Arrays.asList(interfaces));
/*        for (Class cc : interfaces) {
            result.addAll(getInterfaces(cc));
        }*/
        return result;
    }

    public static void setFieldByProperty(Object obj, Field f, Object property) throws Throwable {
        f.setAccessible(true);
        if (f.getType() == int.class || f.getType() == Integer.class) {
            f.set(obj, Integer.parseInt(property.toString()));
        } else {
            f.set(obj, property);
        }
    }

    public static URL getClassPathResource(String resource) {
        return Thread.currentThread().getContextClassLoader().getResource(resource);
    }

    public static InputStream getClassPathResourceAsInputStream(String resource) throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null) throw new IOException("resource not found");
        return url.openStream();
    }

    public static void main(String[] args) {

    }
}
